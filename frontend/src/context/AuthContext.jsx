import { createContext, useState, useEffect, useCallback } from 'react'
import { authService } from '../services/authService'
import toast from 'react-hot-toast'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  // Check for existing session on mount
  useEffect(() => {
    const initAuth = async () => {
      try {
        const storedUser = authService.getStoredUser()
        const token = authService.getAccessToken()

        if (storedUser && token && !authService.isTokenExpired()) {
          setUser(storedUser)
          setIsAuthenticated(true)
        } else if (token && authService.isTokenExpired()) {
          // Try to refresh token
          const refreshToken = localStorage.getItem('refreshToken')
          if (refreshToken) {
            try {
              const response = await authService.refreshToken(refreshToken)
              const { accessToken, refreshToken: newRefreshToken, user: userData } = response.data
              localStorage.setItem('accessToken', accessToken)
              localStorage.setItem('refreshToken', newRefreshToken)
              localStorage.setItem('user', JSON.stringify(userData))
              setUser(userData)
              setIsAuthenticated(true)
            } catch {
              // Refresh failed, clear auth
              await logout()
            }
          }
        }
      } catch (error) {
        console.error('Auth initialization error:', error)
      } finally {
        setLoading(false)
      }
    }

    initAuth()
  }, [])

  // Token expiry checker
  useEffect(() => {
    if (!isAuthenticated) return

    const checkTokenExpiry = setInterval(() => {
      if (authService.isTokenExpired()) {
        toast.error('Session expired. Please login again.')
        logout()
      }
    }, 60000) // Check every minute

    return () => clearInterval(checkTokenExpiry)
  }, [isAuthenticated])

  const login = useCallback(async (username, password) => {
    try {
      const response = await authService.login(username, password)
      const { accessToken, refreshToken, user: userData } = response.data

      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(userData))

      setUser(userData)
      setIsAuthenticated(true)
      toast.success('Login successful!')
      
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed'
      toast.error(message)
      return { success: false, error: message }
    }
  }, [])

  const signup = useCallback(async (username, email, password, displayName) => {
    try {
      const response = await authService.signup(username, email, password, displayName)
      const { accessToken, refreshToken, user: userData } = response.data

      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(userData))

      setUser(userData)
      setIsAuthenticated(true)
      toast.success('Account created successfully!')
      
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || 'Signup failed'
      toast.error(message)
      return { success: false, error: message }
    }
  }, [])

  const logout = useCallback(async () => {
    await authService.logout()
    setUser(null)
    setIsAuthenticated(false)
    toast.success('Logged out successfully')
  }, [])

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    signup,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
