import { useAuth } from '../../hooks/useAuth'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'

function Navbar() {
  const { user, logout } = useAuth()
  const { wsConnected, unreadCount } = useChat()

  return (
    <nav className="bg-gradient-to-r from-primary-600 to-primary-500 px-4 py-3 shadow-md">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <img 
              src="/logo.png"
              alt="Chatty Logo" 
              className="w-8 h-8 rounded-full object-cover"
            />
            <h1 className="text-xl font-bold text-white">Chatty</h1>
          </div>
          <div className="flex items-center space-x-2">
            <span
              className={`w-2 h-2 rounded-full ${
                wsConnected ? 'bg-green-400' : 'bg-yellow-400'
              }`}
            ></span>
            <span className="text-sm text-primary-100">
              {wsConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          {unreadCount > 0 && (
            <span className="bg-secondary-400 text-primary-900 text-xs font-semibold px-2 py-1 rounded-full">
              {unreadCount} unread
            </span>
          )}
          
          <div className="flex items-center space-x-2">
            {user?.profilePicture ? (
              <img
                src={resolveAvatarUrl(user.profilePicture)}
                alt={user.displayName || user.username}
                className="w-8 h-8 rounded-full object-cover border-2 border-white/30"
              />
            ) : (
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center text-white font-medium border-2 border-white/30">
                {user?.displayName?.charAt(0) || user?.username?.charAt(0) || 'U'}
              </div>
            )}
            <span className="text-sm font-medium text-white">
              {user?.displayName || user?.username}
            </span>
          </div>

          <button
            onClick={logout}
            className="text-sm text-white/80 hover:text-white px-3 py-1 rounded-md hover:bg-white/10 transition-colors"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
