import { useState } from 'react'
import { useChat } from '../../hooks/useChat'

function JoinPrivateRoomModal({ onClose }) {
  const [secretCode, setSecretCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  
  const { joinRoomByCode, selectRoom } = useChat()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    const code = secretCode.trim().toUpperCase()
    
    if (!code) {
      setError('Please enter a secret code')
      return
    }
    
    if (code.length !== 8) {
      setError('Secret code must be 8 characters')
      return
    }

    setLoading(true)

    try {
      const room = await joinRoomByCode(code)
      if (room) {
        selectRoom(room)
      }
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid secret code. Please check and try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleCodeChange = (e) => {
    // Only allow alphanumeric characters and convert to uppercase
    const value = e.target.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 8)
    setSecretCode(value)
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Join Private Room</h2>
          <p className="text-sm text-gray-500 mt-1">
            Enter the secret code shared by the room creator
          </p>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="bg-red-50 text-red-500 p-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div>
            <label htmlFor="secretCode" className="block text-sm font-medium text-gray-700 mb-2">
              Secret Code
            </label>
            <input
              id="secretCode"
              type="text"
              value={secretCode}
              onChange={handleCodeChange}
              placeholder="Enter 8-character code"
              className="w-full px-4 py-3 text-center text-2xl tracking-widest font-mono border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none uppercase"
              maxLength={8}
              autoComplete="off"
              autoFocus
            />
            <p className="text-xs text-gray-400 mt-2 text-center">
              Example: A3X7K9M2
            </p>
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || secretCode.length !== 8}
              className="flex-1 px-4 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="flex items-center justify-center">
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Joining...
                </span>
              ) : (
                'Join Room'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default JoinPrivateRoomModal
