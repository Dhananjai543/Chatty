import { useState, useRef } from 'react'
import { useChat } from '../../hooks/useChat'

function MessageInput() {
  const [message, setMessage] = useState('')
  const { sendMessage, currentRoom, currentPrivateChat, wsConnected } = useChat()
  const inputRef = useRef(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    
    if (!message.trim() || !wsConnected) return
    
    sendMessage(message.trim())
    setMessage('')
    inputRef.current?.focus()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSubmit(e)
    }
  }

  const isDisabled = !wsConnected || (!currentRoom && !currentPrivateChat)

  return (
    <div className="px-6 py-4 bg-white border-t border-gray-200">
      <form onSubmit={handleSubmit} className="flex items-end space-x-4">
        <div className="flex-1 relative">
          <textarea
            ref={inputRef}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={
              isDisabled 
                ? 'Select a chat to start messaging...' 
                : 'Type a message...'
            }
            disabled={isDisabled}
            rows={1}
            className="w-full px-4 py-3 bg-gray-100 rounded-2xl resize-none focus:outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ maxHeight: '120px', minHeight: '48px' }}
          />
        </div>
        
        <button
          type="submit"
          disabled={isDisabled || !message.trim()}
          className="p-3 bg-primary-600 text-white rounded-full hover:bg-primary-700 focus:ring-4 focus:ring-primary-200 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </button>
      </form>
      
      {!wsConnected && (
        <p className="text-xs text-red-500 mt-2">
          Disconnected from server. Reconnecting...
        </p>
      )}
    </div>
  )
}

export default MessageInput
