import { Client } from '@stomp/stompjs'

// Use relative URL in development (goes through Vite proxy), absolute URL in production
const WS_URL = import.meta.env.VITE_WS_URL || (import.meta.env.DEV ? '/ws' : 'http://localhost:8080/ws')

// Dynamic import SockJS to avoid SSR issues
let SockJS = null
if (typeof window !== 'undefined') {
  import('sockjs-client').then(module => {
    SockJS = module.default
  })
}

class WebSocketService {
  constructor() {
    this.client = null
    this.subscriptions = new Map()
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.listeners = new Set()
  }

  async connect(token, onConnected, onError) {
    if (this.client?.connected) {
      onConnected?.()
      return
    }

    // Wait for SockJS to load if not already
    if (!SockJS) {
      try {
        const module = await import('sockjs-client')
        SockJS = module.default
      } catch (e) {
        console.error('Failed to load SockJS:', e)
        onError?.(e)
        return
      }
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('STOMP:', str)
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected = true
        this.reconnectAttempts = 0
        console.log('WebSocket connected')
        onConnected?.()
        this.notifyListeners({ type: 'connected' })
      },
      onDisconnect: () => {
        this.connected = false
        console.log('WebSocket disconnected')
        this.notifyListeners({ type: 'disconnected' })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers.message)
        onError?.(frame.headers.message)
        this.notifyListeners({ type: 'error', error: frame.headers.message })
      },
      onWebSocketError: (event) => {
        console.error('WebSocket error:', event)
        onError?.(event)
      },
    })

    this.client.activate()
  }

  disconnect() {
    if (this.client) {
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe()
      })
      this.subscriptions.clear()
      this.client.deactivate()
      this.connected = false
    }
  }

  subscribeToPublicRoom(roomId, callback) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected')
      return null
    }

    const destination = `/topic/public.${roomId}`
    
    if (this.subscriptions.has(destination)) {
      return this.subscriptions.get(destination)
    }

    const subscription = this.client.subscribe(destination, (message) => {
      const body = JSON.parse(message.body)
      callback(body)
    })

    this.subscriptions.set(destination, subscription)
    return subscription
  }

  subscribeToPrivateMessages(userId, callback) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected')
      return null
    }

    const destination = `/user/${userId}/queue/private`
    
    if (this.subscriptions.has(destination)) {
      return this.subscriptions.get(destination)
    }

    const subscription = this.client.subscribe(destination, (message) => {
      const body = JSON.parse(message.body)
      callback(body)
    })

    this.subscriptions.set(destination, subscription)
    return subscription
  }

  subscribeToNotifications(callback) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected')
      return null
    }

    const destination = '/topic/notifications'
    
    if (this.subscriptions.has(destination)) {
      return this.subscriptions.get(destination)
    }

    const subscription = this.client.subscribe(destination, (message) => {
      const body = JSON.parse(message.body)
      callback(body)
    })

    this.subscriptions.set(destination, subscription)
    return subscription
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
    }
  }

  sendPublicMessage(roomId, content) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected')
      return
    }

    this.client.publish({
      destination: `/app/chat.public.${roomId}`,
      body: JSON.stringify({
        chatRoomId: roomId,
        content,
        messageType: 'TEXT',
      }),
    })
  }

  sendPrivateMessage(recipientId, content) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected')
      return
    }

    this.client.publish({
      destination: `/app/chat.private.${recipientId}`,
      body: JSON.stringify({
        recipientId,
        content,
        messageType: 'TEXT',
        isPrivate: true,
      }),
    })
  }

  sendTypingIndicator(roomId) {
    if (!this.client?.connected) return

    this.client.publish({
      destination: `/app/chat.typing.${roomId}`,
      body: JSON.stringify({
        chatRoomId: roomId,
        content: 'typing',
      }),
    })
  }

  addListener(callback) {
    this.listeners.add(callback)
    return () => this.listeners.delete(callback)
  }

  notifyListeners(event) {
    this.listeners.forEach((callback) => callback(event))
  }

  isConnected() {
    return this.connected
  }
}

// Singleton instance
const websocketService = new WebSocketService()
export default websocketService
