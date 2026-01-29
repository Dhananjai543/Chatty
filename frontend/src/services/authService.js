import api from './api'

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password })
    return response.data
  },

  async signup(username, email, password, displayName) {
    const response = await api.post('/auth/signup', {
      username,
      email,
      password,
      displayName: displayName || username,
    })
    return response.data
  },

  async refreshToken(refreshToken) {
    const response = await api.post('/auth/refresh', { refreshToken })
    return response.data
  },

  async logout() {
    try {
      await api.post('/auth/logout')
    } catch (error) {
      // Ignore errors on logout
    }
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  },

  async getCurrentUser() {
    const response = await api.get('/users/me')
    return response.data
  },

  getStoredUser() {
    const user = localStorage.getItem('user')
    return user ? JSON.parse(user) : null
  },

  getAccessToken() {
    return localStorage.getItem('accessToken')
  },

  isTokenExpired() {
    const token = localStorage.getItem('accessToken')
    if (!token) return true

    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.exp * 1000 < Date.now()
    } catch {
      return true
    }
  },
}
