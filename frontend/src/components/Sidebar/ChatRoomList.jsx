import { useChat } from '../../hooks/useChat'

function ChatRoomList({ onCreateRoom }) {
  const { rooms, currentRoom, selectRoom } = useChat()

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="px-4 py-3 flex items-center justify-between border-b border-gray-100">
        <h3 className="text-sm font-semibold text-gray-700">Rooms</h3>
        <button
          onClick={onCreateRoom}
          className="p-1 text-primary-600 hover:bg-primary-50 rounded transition-colors"
          title="Create new room"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </button>
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
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center text-white font-medium ${
                    currentRoom?.id === room.id ? 'bg-primary-600' : 'bg-gray-400'
                  }`}>
                    #
                  </div>
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
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export default ChatRoomList
