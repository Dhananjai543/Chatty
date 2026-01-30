import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './hooks/useAuth'
import AuthPage from './components/Auth/AuthPage'
import ChatLayout from './components/Chat/ChatLayout'
import ProtectedRoute from './components/Common/ProtectedRoute'

function App() {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  return (
    <Routes>
      <Route 
        path="/login" 
        element={isAuthenticated ? <Navigate to="/chat" replace /> : <AuthPage />} 
      />
      <Route 
        path="/signup" 
        element={isAuthenticated ? <Navigate to="/chat" replace /> : <AuthPage />} 
      />
      <Route 
        path="/chat/*" 
        element={
          <ProtectedRoute>
            <ChatLayout />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/" 
        element={<Navigate to={isAuthenticated ? "/chat" : "/login"} replace />} 
      />
      <Route 
        path="*" 
        element={<Navigate to="/" replace />} 
      />
    </Routes>
  )
}

export default App
