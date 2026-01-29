import { useState, useEffect, useCallback } from 'react'
import websocketService from '../services/websocketService'

export function useWebSocket() {
  const [connected, setConnected] = useState(websocketService.isConnected())

  useEffect(() => {
    const unsubscribe = websocketService.addListener((event) => {
      if (event.type === 'connected') {
        setConnected(true)
      } else if (event.type === 'disconnected') {
        setConnected(false)
      }
    })

    return () => unsubscribe()
  }, [])

  const subscribe = useCallback((destination, callback) => {
    if (destination.startsWith('/topic/public.')) {
      const roomId = destination.replace('/topic/public.', '')
      return websocketService.subscribeToPublicRoom(roomId, callback)
    } else if (destination.startsWith('/user/')) {
      const userId = destination.split('/')[2]
      return websocketService.subscribeToPrivateMessages(userId, callback)
    } else if (destination === '/topic/notifications') {
      return websocketService.subscribeToNotifications(callback)
    }
    return null
  }, [])

  const unsubscribe = useCallback((destination) => {
    websocketService.unsubscribe(destination)
  }, [])

  const sendPublicMessage = useCallback((roomId, content) => {
    websocketService.sendPublicMessage(roomId, content)
  }, [])

  const sendPrivateMessage = useCallback((recipientId, content) => {
    websocketService.sendPrivateMessage(recipientId, content)
  }, [])

  return {
    connected,
    subscribe,
    unsubscribe,
    sendPublicMessage,
    sendPrivateMessage,
  }
}
