import { useAuth } from '../../hooks/useAuth'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'

function Navbar() {
  const { user, logout } = useAuth()
  const { wsConnected, unreadCount } = useChat()

  return (
    <nav className="bg-white border-b border-gray-200 px-4 py-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <h1 className="text-xl font-bold text-primary-600">Chatty</h1>
          <div className="flex items-center space-x-2">
            <span
              className={`w-2 h-2 rounded-full ${
                wsConnected ? 'bg-green-500' : 'bg-red-500'
              }`}
            ></span>
            <span className="text-sm text-gray-500">
              {wsConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          {unreadCount > 0 && (
            <span className="bg-red-500 text-white text-xs px-2 py-1 rounded-full">
              {unreadCount} unread
            </span>
          )}
          
          <div className="flex items-center space-x-2">
            {user?.profilePicture ? (
              <img
                src={resolveAvatarUrl(user.profilePicture)}
                alt={user.displayName || user.username}
                className="w-8 h-8 rounded-full object-cover"
              />
            ) : (
              <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center text-white font-medium">
                {user?.displayName?.charAt(0) || user?.username?.charAt(0) || 'U'}
              </div>
            )}
            <span className="text-sm font-medium text-gray-700">
              {user?.displayName || user?.username}
            </span>
          </div>

          <button
            onClick={logout}
            className="text-sm text-gray-500 hover:text-gray-700 px-3 py-1 rounded-md hover:bg-gray-100 transition-colors"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
