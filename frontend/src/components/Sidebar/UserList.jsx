import { useState } from 'react'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'

function UserList() {
  const { users, onlineUsers, currentPrivateChat, selectPrivateChat } = useChat()
  const [searchTerm, setSearchTerm] = useState('')

  const isOnline = (userId) => {
    return onlineUsers.some((u) => u.id === userId)
  }

  const filteredUsers = users.filter((user) =>
    user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.displayName?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  // Sort users: online first, then alphabetically
  const sortedUsers = [...filteredUsers].sort((a, b) => {
    const aOnline = isOnline(a.id)
    const bOnline = isOnline(b.id)
    if (aOnline && !bOnline) return -1
    if (!aOnline && bOnline) return 1
    return (a.displayName || a.username).localeCompare(b.displayName || b.username)
  })

  return (
    <div className="h-full flex flex-col">
      {/* Search */}
      <div className="px-4 py-3 border-b border-primary-100 bg-white">
        <div className="relative">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search users..."
            className="w-full pl-9 pr-4 py-2 bg-gray-100 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white"
          />
          <svg
            className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {sortedUsers.length === 0 ? (
          <div className="px-4 py-8 text-center text-gray-500">
            <p className="text-sm">
              {searchTerm ? 'No users found' : 'No other users yet'}
            </p>
          </div>
        ) : (
          <ul className="py-2">
            {sortedUsers.map((user) => {
              const online = isOnline(user.id)
              const isSelected = currentPrivateChat?.id === user.id

              return (
                <li key={user.id}>
                  <button
                    onClick={() => selectPrivateChat(user)}
                    className={`w-full px-4 py-3 flex items-center space-x-3 hover:bg-primary-100 transition-colors ${
                      isSelected ? 'bg-primary-100' : ''
                    }`}
                  >
                    {/* Avatar with online indicator */}
                    <div className="relative">
                      {user.profilePicture ? (
                        <img
                          src={resolveAvatarUrl(user.profilePicture)}
                          alt={user.displayName || user.username}
                          className="w-10 h-10 rounded-full object-cover"
                        />
                      ) : (
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium ${
                          isSelected ? 'bg-primary-600' : 'bg-gray-400'
                        }`}>
                          {user.displayName?.charAt(0) || user.username.charAt(0)}
                        </div>
                      )}
                      <span
                        className={`absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white ${
                          online ? 'bg-green-500' : 'bg-gray-300'
                        }`}
                      ></span>
                    </div>

                    {/* User Info */}
                    <div className="flex-1 text-left">
                      <p className={`font-medium ${
                        isSelected ? 'text-primary-600' : 'text-gray-900'
                      }`}>
                        {user.displayName || user.username}
                      </p>
                      <p className="text-xs text-gray-500">
                        @{user.username}
                      </p>
                    </div>

                    {/* Online status */}
                    <span className={`text-xs ${online ? 'text-green-500' : 'text-gray-400'}`}>
                      {online ? 'Online' : 'Offline'}
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        )}
      </div>

      {/* Online count */}
      <div className="px-4 py-2 border-t border-primary-100 bg-primary-50">
        <p className="text-xs text-gray-500">
          {onlineUsers.length} user{onlineUsers.length !== 1 ? 's' : ''} online
        </p>
      </div>
    </div>
  )
}

export default UserList
