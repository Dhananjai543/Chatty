import { useState, useEffect } from 'react'
import { useChat } from '../../hooks/useChat'
import { chatService } from '../../services/chatService'

function BrowseRoomsModal({ onClose }) {
  const [publicRooms, setPublicRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [joiningRoomId, setJoiningRoomId] = useState(null)
  const [error, setError] = useState('')
  
  const { rooms, joinRoom, selectRoom } = useChat()

  // Get IDs of rooms user is already a member of
  const memberRoomIds = new Set(rooms.map(r => r.id))

  useEffect(() => {
    loadPublicRooms()
  }, [])

  const loadPublicRooms = async () => {
    try {
      setLoading(true)
      const response = await chatService.getPublicRooms()
      setPublicRooms(response.data || [])
    } catch (err) {
      setError('Failed to load public rooms')
      console.error('Failed to load public rooms:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleJoin = async (room) => {
    setJoiningRoomId(room.id)
    setError('')
    
    try {
      await joinRoom(room.id)
      // Select the room after joining
      selectRoom(room)
      onClose()
    } catch (err) {
      setError('Failed to join room')
    } finally {
      setJoiningRoomId(null)
    }
  }

  // Filter out rooms user is already a member of, and exclude "General" room (always available)
  const availableRooms = publicRooms.filter(room => 
    !memberRoomIds.has(room.id) && room.name !== 'General'
  )

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 overflow-hidden max-h-[80vh] flex flex-col">
        <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Browse Public Rooms</h2>
          <button
            onClick={onClose}
            className="p-1 text-gray-400 hover:text-gray-600 rounded transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-4">
          {error && (
            <div className="bg-red-50 text-red-500 p-3 rounded-lg text-sm mb-4">
              {error}
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            </div>
          ) : availableRooms.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <svg className="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              <p className="font-medium">No new rooms available</p>
              <p className="text-sm mt-1">You&apos;ve already joined all public rooms!</p>
            </div>
          ) : (
            <ul className="space-y-3">
              {availableRooms.map((room) => (
                <li
                  key={room.id}
                  className="border border-gray-200 rounded-lg p-4 hover:border-primary-300 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex items-start space-x-3 flex-1 min-w-0">
                      <div className="w-10 h-10 rounded-lg bg-primary-100 flex items-center justify-center text-primary-600 font-medium flex-shrink-0">
                        #
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-medium text-gray-900 truncate">{room.name}</h3>
                        {room.description && (
                          <p className="text-sm text-gray-500 mt-0.5 line-clamp-2">{room.description}</p>
                        )}
                        <div className="flex items-center space-x-3 mt-2 text-xs text-gray-400">
                          <span className="flex items-center">
                            <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                            </svg>
                            {room.memberCount} members
                          </span>
                        </div>
                      </div>
                    </div>
                    <button
                      onClick={() => handleJoin(room)}
                      disabled={joiningRoomId === room.id}
                      className="ml-4 px-4 py-2 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex-shrink-0"
                    >
                      {joiningRoomId === room.id ? (
                        <span className="flex items-center">
                          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Joining...
                        </span>
                      ) : (
                        'Join'
                      )}
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="px-6 py-4 border-t border-gray-200 bg-gray-50">
          <button
            onClick={onClose}
            className="w-full px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  )
}

export default BrowseRoomsModal
