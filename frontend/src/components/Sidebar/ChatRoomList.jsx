import { useState } from 'react'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'
import BrowseRoomsModal from '../Chat/BrowseRoomsModal'
import JoinPrivateRoomModal from '../Chat/JoinPrivateRoomModal'

function ChatRoomList({ onCreateRoom }) {
  const { rooms, currentRoom, selectRoom } = useChat()
  const [showBrowseRooms, setShowBrowseRooms] = useState(false)
  const [showJoinPrivate, setShowJoinPrivate] = useState(false)

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="px-4 py-3 flex items-center justify-between border-b border-gray-100">
        <h3 className="text-sm font-semibold text-gray-700">Rooms</h3>
        <div className="flex items-center space-x-1">
          {/* Create Room Button */}
          <button
            onClick={onCreateRoom}
            className="p-1.5 text-primary-600 hover:bg-primary-50 rounded transition-colors"
            title="Create new room"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
          </button>
          
          {/* Browse Public Rooms Button */}
          <button
            onClick={() => setShowBrowseRooms(true)}
            className="p-1.5 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors"
            title="Browse public rooms"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
          
          {/* Join Private Room Button */}
          <button
            onClick={() => setShowJoinPrivate(true)}
            className="p-1.5 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors"
            title="Join private room with code"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
          </button>
        </div>
      </div>

      {/* Room List */}
      <div className="flex-1 overflow-y-auto">
        {rooms.length === 0 ? (
          <div className="px-4 py-8 text-center text-gray-500">
            <p className="text-sm">No rooms available</p>
            <button
              onClick={onCreateRoom}
              className="mt-2 text-primary-600 text-sm font-medium hover:underline"
            >
              Create one
            </button>
          </div>
        ) : (
          <ul className="py-2">
            {rooms.map((room) => (
              <li key={room.id}>
                <button
                  onClick={() => selectRoom(room)}
                  className={`w-full px-4 py-3 flex items-center space-x-3 hover:bg-gray-50 transition-colors ${
                    currentRoom?.id === room.id ? 'bg-primary-50' : ''
                  }`}
                >
                  {room.profilePicture ? (
                    <img
                      src={resolveAvatarUrl(room.profilePicture)}
                      alt={room.name}
                      className="w-10 h-10 rounded-lg object-cover"
                    />
                  ) : (
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center text-white font-medium ${
                      currentRoom?.id === room.id ? 'bg-primary-600' : 'bg-gray-400'
                    }`}>
                      #
                    </div>
                  )}
                  <div className="flex-1 text-left">
                    <p className={`font-medium ${
                      currentRoom?.id === room.id ? 'text-primary-600' : 'text-gray-900'
                    }`}>
                      {room.name}
                    </p>
                    {room.description && (
                      <p className="text-xs text-gray-500 truncate">
                        {room.description}
                      </p>
                    )}
                  </div>
                  {room.memberCount > 0 && (
                    <span className="text-xs text-gray-400">
                      {room.memberCount}
                    </span>
                  )}
                  {/* Private room indicator */}
                  {!room.isPublic && (
                    <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" title="Private room">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  )}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Modals */}
      {showBrowseRooms && (
        <BrowseRoomsModal onClose={() => setShowBrowseRooms(false)} />
      )}
      {showJoinPrivate && (
        <JoinPrivateRoomModal onClose={() => setShowJoinPrivate(false)} />
      )}
    </div>
  )
}

export default ChatRoomList
