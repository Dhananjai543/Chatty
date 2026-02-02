import { useAuth } from '../../hooks/useAuth'
import { useChat } from '../../hooks/useChat'
import { resolveAvatarUrl } from '../../services/api'

function MessageList() {
  const { user } = useAuth()
  const { messages } = useChat()

  const formatTime = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: true 
    })
  }

  const formatDate = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    if (date.toDateString() === today.toDateString()) {
      return 'Today'
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Yesterday'
    }
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric',
      year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
    })
  }

  const groupMessagesByDate = (messages) => {
    const groups = {}
    messages.forEach((message) => {
      const date = formatDate(message.timestamp)
      if (!groups[date]) {
        groups[date] = []
      }
      groups[date].push(message)
    })
    return groups
  }

  const groupedMessages = groupMessagesByDate(messages)

  if (messages.length === 0) {
    return (
      <div className="flex items-center justify-center h-full text-gray-500">
        <p>No messages yet. Start the conversation!</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {Object.entries(groupedMessages).map(([date, dateMessages]) => (
        <div key={date}>
          {/* Date Separator */}
          <div className="flex items-center justify-center my-4">
            <div className="bg-gray-200 text-gray-600 text-xs px-3 py-1 rounded-full">
              {date}
            </div>
          </div>

          {/* Messages */}
          <div className="space-y-3">
            {dateMessages.map((message, index) => {
              const isOwnMessage = message.senderId === user?.id || 
                                   message.senderUsername === user?.username
              const showAvatar = index === 0 || 
                                 dateMessages[index - 1]?.senderId !== message.senderId

              return (
                <div
                  key={message.id || index}
                  className={`flex ${isOwnMessage ? 'justify-end' : 'justify-start'} message-enter`}
                >
                  <div className={`flex items-end space-x-2 max-w-[70%] ${
                    isOwnMessage ? 'flex-row-reverse space-x-reverse' : ''
                  }`}>
                    {/* Avatar */}
                    {!isOwnMessage && showAvatar && (
                      message.senderProfilePicture ? (
                        <img
                          src={resolveAvatarUrl(message.senderProfilePicture)}
                          alt={message.senderDisplayName || message.senderUsername}
                          className="w-8 h-8 rounded-full object-cover flex-shrink-0"
                        />
                      ) : (
                        <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center text-gray-600 text-sm font-medium flex-shrink-0">
                          {message.senderDisplayName?.charAt(0) || message.senderUsername?.charAt(0) || '?'}
                        </div>
                      )
                    )}
                    {!isOwnMessage && !showAvatar && (
                      <div className="w-8 flex-shrink-0"></div>
                    )}

                    {/* Message Bubble */}
                    <div className={`rounded-2xl px-4 py-2 ${
                      isOwnMessage 
                        ? 'bg-primary-600 text-white rounded-br-md' 
                        : 'bg-white text-gray-900 rounded-bl-md shadow-sm'
                    }`}>
                      {/* Sender Name (for group chats) */}
                      {!isOwnMessage && showAvatar && (
                        <p className="text-xs font-medium text-primary-600 mb-1">
                          {message.senderDisplayName || message.senderUsername}
                        </p>
                      )}
                      
                      {/* Message Content */}
                      <p className="text-sm whitespace-pre-wrap break-words">
                        {message.content}
                      </p>
                      
                      {/* Timestamp */}
                      <p className={`text-xs mt-1 ${
                        isOwnMessage ? 'text-primary-200' : 'text-gray-400'
                      }`}>
                        {formatTime(message.timestamp)}
                      </p>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      ))}
    </div>
  )
}

export default MessageList
