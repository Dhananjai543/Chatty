import api from './api'

export const chatService = {
  // Chat Rooms
  async getRooms() {
    const response = await api.get('/chat/rooms')
    return response.data
  },

  async getPublicRooms() {
    const response = await api.get('/chat/rooms/public')
    return response.data
  },

  async getRoomById(roomId) {
    const response = await api.get(`/chat/rooms/${roomId}`)
    return response.data
  },

  async createRoom(name, description, isPublic = true) {
    const response = await api.post('/chat/rooms', {
      name,
      description,
      isPublic,
    })
    return response.data
  },

  async joinRoom(roomId) {
    const response = await api.post(`/chat/rooms/${roomId}/join`)
    return response.data
  },

  async leaveRoom(roomId) {
    const response = await api.post(`/chat/rooms/${roomId}/leave`)
    return response.data
  },

  async joinRoomByCode(secretCode) {
    const response = await api.post('/chat/rooms/join-by-code', { secretCode })
    return response.data
  },

  // Messages
  async getRoomMessages(roomId, page = 0, size = 50) {
    const response = await api.get(`/chat/rooms/${roomId}/messages`, {
      params: { page, size },
    })
    return response.data
  },

  async getPrivateMessages(userId, page = 0, size = 50) {
    const response = await api.get(`/chat/private/${userId}/messages`, {
      params: { page, size },
    })
    return response.data
  },

  async markMessagesAsRead(userId) {
    const response = await api.post(`/chat/private/${userId}/read`)
    return response.data
  },

  async getUnreadCount() {
    const response = await api.get('/chat/unread/count')
    return response.data
  },

  // Users
  async getAllUsers() {
    const response = await api.get('/users')
    return response.data
  },

  async getOnlineUsers() {
    const response = await api.get('/users/online')
    return response.data
  },

  async getUserById(userId) {
    const response = await api.get(`/users/${userId}`)
    return response.data
  },
}
