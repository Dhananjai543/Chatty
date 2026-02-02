import { createContext, useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '../hooks/useAuth'
import { chatService } from '../services/chatService'
import websocketService from '../services/websocketService'
import toast from 'react-hot-toast'

export const ChatContext = createContext(null)

export function ChatProvider({ children }) {
  const { user, isAuthenticated } = useAuth()
  const [rooms, setRooms] = useState([])
  const [users, setUsers] = useState([])
  const [onlineUsers, setOnlineUsers] = useState([])
  const [currentRoom, setCurrentRoom] = useState(null)
  const [currentPrivateChat, setCurrentPrivateChat] = useState(null)
  const [messages, setMessages] = useState([])
  const [wsConnected, setWsConnected] = useState(false)
  const [loading, setLoading] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  
  const subscriptionsRef = useRef(new Set())
  const currentPrivateChatRef = useRef(null)

  // Connect WebSocket when authenticated
  useEffect(() => {
    if (isAuthenticated && user) {
      const token = localStorage.getItem('accessToken')
      
      websocketService.connect(
        token,
        () => {
          setWsConnected(true)
          setupSubscriptions()
        },
        (error) => {
          console.error('WebSocket error:', error)
          toast.error('Connection error. Retrying...')
        }
      )

      // Load initial data
      loadRooms()
      loadUsers()
      loadUnreadCount()
    }

    return () => {
      websocketService.disconnect()
      setWsConnected(false)
    }
  }, [isAuthenticated, user])

  // Keep ref in sync with state to avoid stale closures in callbacks
  useEffect(() => {
    currentPrivateChatRef.current = currentPrivateChat
  }, [currentPrivateChat])

  const setupSubscriptions = useCallback(() => {
    if (!user) return

    // Subscribe to notifications
    websocketService.subscribeToNotifications((notification) => {
      if (notification.messageType === 'JOIN') {
        setOnlineUsers((prev) => {
          const exists = prev.some((u) => u.username === notification.senderUsername)
          if (!exists && notification.senderUsername !== user.username) {
            toast.success(`${notification.senderUsername} is now online`)
          }
          return prev
        })
        loadUsers()
      } else if (notification.messageType === 'LEAVE') {
        loadUsers()
      }
    })

    // Subscribe to private messages - Spring routes based on authenticated Principal
    websocketService.subscribeToPrivateMessages((message) => {
      console.log('Private message received:', message)
      console.log('Current active chat ref:', currentPrivateChatRef.current)
      
      // Use ref to get current value (avoids stale closure)
      const activeChat = currentPrivateChatRef.current
      
      if (activeChat) {
        // Check if this message belongs to the current private chat
        // Either we sent it (to activeChat) or received it (from activeChat)
        const isForCurrentChat = 
          (message.senderId === activeChat.id) ||      // Message from the person we're chatting with
          (message.recipientId === activeChat.id) ||   // Message to the person we're chatting with
          (message.senderUsername === activeChat.username) ||
          (message.recipientUsername === activeChat.username)
        
        console.log('Is for current chat:', isForCurrentChat, 'activeChat.id:', activeChat.id, 'activeChat.username:', activeChat.username)
        
        if (isForCurrentChat) {
          setMessages((prev) => {
            // Avoid duplicates by checking if message already exists
            if (prev.some(m => m.id === message.id)) {
              return prev
            }
            return [...prev, message]
          })
          return
        }
      }
      
      // Message not for current chat - show notification if from someone else
      if (message.senderId !== user.id) {
        toast(`New message from ${message.senderDisplayName || message.senderUsername}`)
        setUnreadCount((prev) => prev + 1)
      }
    })
  }, [user])

  const loadRooms = async () => {
    try {
      const response = await chatService.getRooms()
      setRooms(response.data || [])
    } catch (error) {
      console.error('Failed to load rooms:', error)
    }
  }

  const loadUsers = async () => {
    try {
      const [allUsersRes, onlineRes] = await Promise.all([
        chatService.getAllUsers(),
        chatService.getOnlineUsers(),
      ])
      setUsers((allUsersRes.data || []).filter((u) => u.id !== user?.id))
      setOnlineUsers(onlineRes.data || [])
    } catch (error) {
      console.error('Failed to load users:', error)
    }
  }

  const loadUnreadCount = async () => {
    try {
      const response = await chatService.getUnreadCount()
      setUnreadCount(response.data || 0)
    } catch (error) {
      console.error('Failed to load unread count:', error)
    }
  }

  const selectRoom = useCallback(async (room) => {
    setCurrentPrivateChat(null)
    // Clear private chat ref immediately
    currentPrivateChatRef.current = null
    setCurrentRoom(room)
    setLoading(true)
    setMessages([])

    try {
      // Load room messages
      const response = await chatService.getRoomMessages(room.id)
      setMessages(response.data || [])

      // Subscribe to room messages
      const destination = `/topic/public.${room.id}`
      if (!subscriptionsRef.current.has(destination)) {
        websocketService.subscribeToPublicRoom(room.id, (message) => {
          setMessages((prev) => [...prev, message])
        })
        subscriptionsRef.current.add(destination)
      }
    } catch (error) {
      console.error('Failed to load room messages:', error)
      toast.error('Failed to load messages')
    } finally {
      setLoading(false)
    }
  }, [])

  const selectPrivateChat = useCallback(async (targetUser) => {
    setCurrentRoom(null)
    setCurrentPrivateChat(targetUser)
    // Update ref immediately (don't wait for useEffect)
    currentPrivateChatRef.current = targetUser
    console.log('Selected private chat:', targetUser)
    setLoading(true)
    setMessages([])

    try {
      // Load private messages
      const response = await chatService.getPrivateMessages(targetUser.id)
      setMessages(response.data || [])

      // Mark messages as read
      await chatService.markMessagesAsRead(targetUser.id)
      loadUnreadCount()
    } catch (error) {
      console.error('Failed to load private messages:', error)
      toast.error('Failed to load messages')
    } finally {
      setLoading(false)
    }
  }, [])

  const sendMessage = useCallback((content) => {
    if (!content.trim()) return

    if (currentRoom) {
      websocketService.sendPublicMessage(currentRoom.id, content)
    } else if (currentPrivateChat) {
      websocketService.sendPrivateMessage(currentPrivateChat.id, content)
    }
  }, [currentRoom, currentPrivateChat])

  const createRoom = useCallback(async (name, description, isPublic = true) => {
    try {
      const response = await chatService.createRoom(name, description, isPublic)
      await loadRooms()
      toast.success('Room created successfully!')
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create room'
      toast.error(message)
      throw error
    }
  }, [])

  const joinRoom = useCallback(async (roomId) => {
    try {
      const response = await chatService.joinRoom(roomId)
      await loadRooms()
      toast.success('Joined room successfully!')
      return response.data
    } catch (error) {
      toast.error('Failed to join room')
      throw error
    }
  }, [])

  const joinRoomByCode = useCallback(async (secretCode) => {
    try {
      const response = await chatService.joinRoomByCode(secretCode)
      await loadRooms()
      toast.success('Joined room successfully!')
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Invalid secret code'
      toast.error(message)
      throw error
    }
  }, [])

  const value = {
    rooms,
    users,
    onlineUsers,
    currentRoom,
    currentPrivateChat,
    messages,
    wsConnected,
    loading,
    unreadCount,
    selectRoom,
    selectPrivateChat,
    sendMessage,
    createRoom,
    joinRoom,
    joinRoomByCode,
    loadRooms,
    loadUsers,
  }

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>
}
