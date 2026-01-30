import { useState } from 'react'
import Navbar from '../Common/Navbar'
import Sidebar from '../Sidebar/Sidebar'
import ChatWindow from './ChatWindow'
import CreateRoomModal from './CreateRoomModal'
import { useChat } from '../../hooks/useChat'

function ChatLayout() {
  const [showCreateRoom, setShowCreateRoom] = useState(false)
  const { currentRoom, currentPrivateChat } = useChat()

  return (
    <div className="h-screen flex flex-col bg-gray-100">
      <Navbar />
      
      <div className="flex-1 flex overflow-hidden">
        <Sidebar onCreateRoom={() => setShowCreateRoom(true)} />
        
        <main className="flex-1 flex flex-col min-h-0">
          {currentRoom || currentPrivateChat ? (
            <ChatWindow />
          ) : (
            <div className="flex-1 flex items-center justify-center bg-gray-50">
              <div className="text-center">
                <div className="w-24 h-24 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                </div>
                <h3 className="text-xl font-medium text-gray-700 mb-2">Welcome to Chatty</h3>
                <p className="text-gray-500">Select a chat room or start a private conversation</p>
              </div>
            </div>
          )}
        </main>
      </div>

      {showCreateRoom && (
        <CreateRoomModal onClose={() => setShowCreateRoom(false)} />
      )}
    </div>
  )
}

export default ChatLayout
