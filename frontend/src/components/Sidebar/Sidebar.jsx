import { useState } from 'react'
import ChatRoomList from './ChatRoomList'
import UserList from './UserList'

function Sidebar({ onCreateRoom }) {
  const [activeTab, setActiveTab] = useState('rooms')

  return (
    <aside className="w-80 bg-primary-50 border-r border-primary-100 flex flex-col">
      <div className="flex border-b border-primary-100 bg-white">
        <button
          onClick={() => setActiveTab('rooms')}
          className={`flex-1 py-3 text-sm font-medium transition-colors ${
            activeTab === 'rooms'
              ? 'text-primary-600 border-b-2 border-primary-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          Chat Rooms
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={`flex-1 py-3 text-sm font-medium transition-colors ${
            activeTab === 'users'
              ? 'text-primary-600 border-b-2 border-primary-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          Direct Messages
        </button>
      </div>

      {/* Tab Content */}
      <div className="flex-1 overflow-hidden">
        {activeTab === 'rooms' ? (
          <ChatRoomList onCreateRoom={onCreateRoom} />
        ) : (
          <UserList />
        )}
      </div>
    </aside>
  )
}

export default Sidebar
