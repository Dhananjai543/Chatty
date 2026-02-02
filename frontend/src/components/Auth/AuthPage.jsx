import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function AuthPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { login, signup } = useAuth()

  // Determine initial mode based on route
  const [isRightPanelActive, setIsRightPanelActive] = useState(
    location.pathname === '/signup'
  )

  // Sign In form state
  const [signInData, setSignInData] = useState({
    username: '',
    password: '',
  })
  const [signInLoading, setSignInLoading] = useState(false)
  const [signInError, setSignInError] = useState('')

  // Sign Up form state
  const [signUpData, setSignUpData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    displayName: '',
  })
  const [signUpLoading, setSignUpLoading] = useState(false)
  const [signUpError, setSignUpError] = useState('')

  // Update panel state when route changes
  useEffect(() => {
    setIsRightPanelActive(location.pathname === '/signup')
  }, [location.pathname])

  // Update URL when panel changes
  const handleTogglePanel = (showSignUp) => {
    setIsRightPanelActive(showSignUp)
    navigate(showSignUp ? '/signup' : '/login', { replace: true })
  }

  // Sign In handlers
  const handleSignInChange = (e) => {
    setSignInData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }))
  }

  const handleSignInSubmit = async (e) => {
    e.preventDefault()
    setSignInError('')
    setSignInLoading(true)

    const result = await login(signInData.username, signInData.password)

    if (result.success) {
      navigate('/chat')
    } else {
      setSignInError(result.error)
    }

    setSignInLoading(false)
  }

  // Sign Up handlers
  const handleSignUpChange = (e) => {
    setSignUpData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }))
  }

  const handleSignUpSubmit = async (e) => {
    e.preventDefault()
    setSignUpError('')

    // Validation
    if (signUpData.password !== signUpData.confirmPassword) {
      setSignUpError('Passwords do not match')
      return
    }

    if (signUpData.password.length < 6) {
      setSignUpError('Password must be at least 6 characters')
      return
    }

    setSignUpLoading(true)

    const result = await signup(
      signUpData.username,
      signUpData.email,
      signUpData.password,
      signUpData.displayName || signUpData.username
    )

    if (result.success) {
      navigate('/chat')
    } else {
      setSignUpError(result.error)
    }

    setSignUpLoading(false)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-500 to-primary-700 py-8 px-4">
      <div
        className={`auth-container ${isRightPanelActive ? 'right-panel-active' : ''}`}
      >
        {/* Sign Up Form */}
        <div className="form-container sign-up-container">
          <form onSubmit={handleSignUpSubmit} className="auth-form">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Create Account</h1>
            <p className="text-gray-500 text-sm mb-6">Join Chatty today</p>

            {signUpError && (
              <div className="w-full bg-red-50 text-red-500 p-3 rounded-lg text-sm mb-4">
                {signUpError}
              </div>
            )}

            <input
              type="text"
              name="username"
              placeholder="Username"
              required
              value={signUpData.username}
              onChange={handleSignUpChange}
              className="auth-input"
            />
            <input
              type="email"
              name="email"
              placeholder="Email"
              required
              value={signUpData.email}
              onChange={handleSignUpChange}
              className="auth-input"
            />
            <input
              type="text"
              name="displayName"
              placeholder="Display Name (optional)"
              value={signUpData.displayName}
              onChange={handleSignUpChange}
              className="auth-input"
            />
            <input
              type="password"
              name="password"
              placeholder="Password"
              required
              value={signUpData.password}
              onChange={handleSignUpChange}
              className="auth-input"
            />
            <input
              type="password"
              name="confirmPassword"
              placeholder="Confirm Password"
              required
              value={signUpData.confirmPassword}
              onChange={handleSignUpChange}
              className="auth-input"
            />

            <button
              type="submit"
              disabled={signUpLoading}
              className="auth-button mt-4"
            >
              {signUpLoading ? (
                <span className="flex items-center justify-center">
                  <svg
                    className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  Creating...
                </span>
              ) : (
                'Sign Up'
              )}
            </button>

            {/* Mobile toggle link */}
            <p className="mt-4 text-sm text-gray-600 md:hidden">
              Already have an account?{' '}
              <button
                type="button"
                onClick={() => handleTogglePanel(false)}
                className="text-primary-600 font-medium hover:text-primary-700"
              >
                Sign In
              </button>
            </p>
          </form>
        </div>

        {/* Sign In Form */}
        <div className="form-container sign-in-container">
          <form onSubmit={handleSignInSubmit} className="auth-form">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Sign In</h1>
            <p className="text-gray-500 text-sm mb-6">Welcome back to Chatty</p>

            {signInError && (
              <div className="w-full bg-red-50 text-red-500 p-3 rounded-lg text-sm mb-4">
                {signInError}
              </div>
            )}

            <input
              type="text"
              name="username"
              placeholder="Username"
              required
              value={signInData.username}
              onChange={handleSignInChange}
              className="auth-input"
            />
            <input
              type="password"
              name="password"
              placeholder="Password"
              required
              value={signInData.password}
              onChange={handleSignInChange}
              className="auth-input"
            />

            <button
              type="submit"
              disabled={signInLoading}
              className="auth-button mt-4"
            >
              {signInLoading ? (
                <span className="flex items-center justify-center">
                  <svg
                    className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  Signing in...
                </span>
              ) : (
                'Sign In'
              )}
            </button>

            {/* Mobile toggle link */}
            <p className="mt-4 text-sm text-gray-600 md:hidden">
              Don&apos;t have an account?{' '}
              <button
                type="button"
                onClick={() => handleTogglePanel(true)}
                className="text-primary-600 font-medium hover:text-primary-700"
              >
                Sign Up
              </button>
            </p>
          </form>
        </div>

        {/* Overlay */}
        <div className="overlay-container">
          <div className="overlay">
            {/* Left Panel - shown when Sign Up is active */}
            <div className="overlay-panel overlay-left">
              <h1 className="text-3xl font-bold text-white mb-2">Welcome Back!</h1>
              <p className="text-white/80 text-sm mb-6 max-w-xs text-center">
                To keep connected with us please login with your personal info
              </p>
              <button
                type="button"
                className="overlay-button"
                onClick={() => handleTogglePanel(false)}
              >
                Sign In
              </button>
            </div>

            {/* Right Panel - shown when Sign In is active */}
            <div className="overlay-panel overlay-right">
              <h1 className="text-3xl font-bold text-white mb-2">Hello, Friend!</h1>
              <p className="text-white/80 text-sm mb-6 max-w-xs text-center">
                Enter your personal details and start your journey with us
              </p>
              <button
                type="button"
                className="overlay-button"
                onClick={() => handleTogglePanel(true)}
              >
                Sign Up
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AuthPage
