import { useRef, useEffect, useState } from 'react'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'
import MessageList from './MessageList'
import MessageInput from './MessageInput'

function ChatWindow() {
  const { currentRoom, currentPrivateChat, messages, loading } = useChat()
  const messagesContainerRef = useRef(null)
  const [codeCopied, setCodeCopied] = useState(false)
  const [showSecretCode, setShowSecretCode] = useState(false)

  const chatName = currentRoom?.name || currentPrivateChat?.displayName || currentPrivateChat?.username
  const chatDescription = currentRoom?.description || (currentPrivateChat && 'Private conversation')

  // Scroll to bottom when new messages arrive (only scroll the messages container, not the page)
  useEffect(() => {
    if (messagesContainerRef.current && !loading && messages.length > 0) {
      // Use setTimeout to ensure DOM has updated before scrolling
      setTimeout(() => {
        if (messagesContainerRef.current) {
          messagesContainerRef.current.scrollTop = messagesContainerRef.current.scrollHeight
        }
      }, 0)
    }
  }, [messages, currentRoom, currentPrivateChat, loading])

  const handleCopySecretCode = async () => {
    if (currentRoom?.secretCode) {
      try {
        await navigator.clipboard.writeText(currentRoom.secretCode)
        setCodeCopied(true)
        setTimeout(() => setCodeCopied(false), 2000)
      } catch (err) {
        console.error('Failed to copy:', err)
      }
    }
  }

  return (
    <div className="flex-1 flex flex-col bg-white min-h-0">
      <div className="px-6 py-4 border-b border-gray-200 bg-white">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            {currentRoom?.profilePicture ? (
              <img
                src={resolveAvatarUrl(currentRoom.profilePicture)}
                alt={currentRoom.name}
                className="w-10 h-10 rounded-full object-cover"
              />
            ) : currentPrivateChat?.profilePicture ? (
              <img
                src={resolveAvatarUrl(currentPrivateChat.profilePicture)}
                alt={currentPrivateChat.displayName || currentPrivateChat.username}
                className="w-10 h-10 rounded-full object-cover"
              />
            ) : (
              <div className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium ${
                currentRoom ? 'bg-primary-500' : 'bg-green-500'
              }`}>
                {currentRoom ? '#' : chatName?.charAt(0).toUpperCase()}
              </div>
            )}
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="font-semibold text-gray-900">{chatName}</h2>
                {currentRoom && !currentRoom.isPublic && (
                  <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" title="Private room">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                )}
              </div>
              {chatDescription && (
                <p className="text-sm text-gray-500">{chatDescription}</p>
              )}
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            {currentRoom && !currentRoom.isPublic && currentRoom.secretCode && (
              <div className="relative">
                <button
                  onClick={() => setShowSecretCode(!showSecretCode)}
                  className="flex items-center space-x-1 px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                  title="Show invite code"
                >
                  <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                  </svg>
                  <span className="text-gray-600">Invite Code</span>
                </button>
                
                {showSecretCode && (
                  <div className="absolute right-0 top-full mt-2 bg-white border border-gray-200 rounded-lg shadow-lg p-3 z-10 min-w-[200px]">
                    <p className="text-xs text-gray-500 mb-2">Share this code to invite others:</p>
                    <div className="flex items-center space-x-2">
                      <code className="flex-1 text-lg font-mono font-bold tracking-wider text-primary-600 bg-gray-50 px-3 py-1.5 rounded">
                        {currentRoom.secretCode}
                      </code>
                      <button
                        onClick={handleCopySecretCode}
                        className="p-2 text-gray-500 hover:text-primary-600 hover:bg-gray-100 rounded transition-colors"
                        title="Copy code"
                      >
                        {codeCopied ? (
                          <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                          </svg>
                        ) : (
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                          </svg>
                        )}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
            
            {currentRoom && (
              <div className="flex items-center space-x-2 text-sm text-gray-500">
                <span>{currentRoom.memberCount || 0} members</span>
              </div>
            )}
          </div>
        </div>
      </div>

      <div ref={messagesContainerRef} className="flex-1 overflow-y-auto px-6 py-4 bg-gray-50 min-h-0">
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <MessageList />
        )}
      </div>

      <MessageInput />
    </div>
  )
}

export default ChatWindow
