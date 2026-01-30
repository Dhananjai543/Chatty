import { useRef, useEffect } from 'react'
import { useChat } from '../../hooks/useChat'
import MessageList from './MessageList'
import MessageInput from './MessageInput'

function ChatWindow() {
  const { currentRoom, currentPrivateChat, messages, loading } = useChat()
  const messagesContainerRef = useRef(null)

  const chatName = currentRoom?.name || currentPrivateChat?.displayName || currentPrivateChat?.username
  const chatDescription = currentRoom?.description || (currentPrivateChat && 'Private conversation')

  // Scroll to bottom when new messages arrive (only scroll the messages container, not the page)
  useEffect(() => {
    if (messagesContainerRef.current) {
      messagesContainerRef.current.scrollTop = messagesContainerRef.current.scrollHeight
    }
  }, [messages, currentRoom, currentPrivateChat])

  return (
    <div className="flex-1 flex flex-col bg-white min-h-0">
      {/* Chat Header */}
      <div className="px-6 py-4 border-b border-gray-200 bg-white">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium ${
              currentRoom ? 'bg-primary-500' : 'bg-green-500'
            }`}>
              {currentRoom ? '#' : chatName?.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">{chatName}</h2>
              {chatDescription && (
                <p className="text-sm text-gray-500">{chatDescription}</p>
              )}
            </div>
          </div>
          
          {currentRoom && (
            <div className="flex items-center space-x-2 text-sm text-gray-500">
              <span>{currentRoom.memberCount || 0} members</span>
            </div>
          )}
        </div>
      </div>

      {/* Messages Area */}
      <div ref={messagesContainerRef} className="flex-1 overflow-y-auto px-6 py-4 bg-gray-50 min-h-0">
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <MessageList />
        )}
      </div>

      {/* Message Input */}
      <MessageInput />
    </div>
  )
}

export default ChatWindow
