import { createFileRoute, Link, useNavigate } from '@tanstack/react-router'
import {
  KeyRound,
  Lock,
  LogIn,
  Moon,
  ShieldCheck,
  Sun,
  User,
  Wrench,
} from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/login')({ component: LoginPage })

interface LoginResponse {
  success: boolean
  message: string
  username: string
  token: string
}

function LoginPage() {
  const navigate = useNavigate()
  const [identifier, setIdentifier] = React.useState('')
  const [password, setPassword] = React.useState('')
  const [error, setError] = React.useState<string | null>(null)
  const [loading, setLoading] = React.useState(false)
  const [theme, setTheme] = React.useState<'light' | 'dark'>('dark')

  React.useEffect(() => {
    const root = document.documentElement
    const isDark =
      root.classList.contains('dark') ||
      root.getAttribute('data-theme') === 'dark'
    setTheme(isDark ? 'dark' : 'light')
  }, [])

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light'
    const root = document.documentElement
    root.classList.remove('light', 'dark')
    root.classList.add(nextTheme)
    root.setAttribute('data-theme', nextTheme)
    localStorage.setItem('theme', nextTheme)
    setTheme(nextTheme)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    if (!identifier || !password) {
      setError('Please enter your username or email address and password.')
      setLoading(false)
      return
    }

    try {
      const data = await bffFetch<LoginResponse>('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ identifier, password }),
      })

      if (data.token) {
        localStorage.setItem('apu_asc_token', data.token)
        setLoading(false)
        navigate({ to: '/' })
      } else {
        setLoading(false)
        setError(data.message || 'Invalid username or password.')
      }
    } catch (err: unknown) {
      setLoading(false)
      const msg = err instanceof Error ? err.message : 'Authentication failed'
      setError(msg)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors justify-between">
      <header className="px-6 py-4 border-b border-border bg-card">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-primary/10 border border-primary/30 flex items-center justify-center text-primary font-bold">
              <Wrench className="w-4 h-4" />
            </div>
            <span className="font-heading font-bold text-lg tracking-tight">
              APU-ASC
            </span>
          </Link>

          <button
            type="button"
            onClick={toggleTheme}
            className="p-2 rounded-md border border-border bg-card hover:bg-muted text-foreground transition-colors"
            title="Toggle theme"
          >
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-primary" />
            ) : (
              <Moon className="w-4 h-4 text-primary" />
            )}
          </button>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6">
          <div className="space-y-2 text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 border border-primary/30 text-primary mb-2">
              <LogIn className="w-6 h-6" />
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Sign In to APU-ASC
            </h1>
            <p className="text-xs text-muted-foreground">
              Enter your registered credentials or sign in via Keycloak
              Enterprise Identity.
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-status-pending/10 border border-status-pending/30 text-status-pending text-xs font-medium">
              {error}
            </div>
          )}

          <a
            href="/oauth2/authorization/keycloak"
            className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center justify-center gap-2 text-xs shadow-md"
          >
            <ShieldCheck className="w-4 h-4" />
            Sign In with Keycloak SSO (Enterprise)
          </a>

          <div className="relative flex items-center justify-center">
            <div className="border-t border-border w-full" />
            <span className="bg-card px-3 text-[11px] text-muted-foreground uppercase font-semibold">
              Or direct login
            </span>
            <div className="border-t border-border w-full" />
          </div>

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div className="space-y-1.5">
              <label
                htmlFor="login-identifier"
                className="font-semibold text-foreground block"
              >
                Username or Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <User className="w-4 h-4" />
                </div>
                <input
                  id="login-identifier"
                  type="text"
                  required
                  placeholder="admin or manager@apu-asc.com"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 rounded-lg bg-muted border border-border focus:border-primary text-foreground placeholder:text-muted-foreground outline-none transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="login-password"
                className="font-semibold text-foreground block"
              >
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  id="login-password"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 rounded-lg bg-muted border border-border focus:border-primary text-foreground placeholder:text-muted-foreground outline-none transition-colors"
                />
              </div>
              <div className="flex justify-end pt-1">
                <a
                  href="http://localhost/auth/realms/apu-asc/login-actions/reset-credentials"
                  target="_blank"
                  rel="noreferrer"
                  className="text-[11px] font-medium text-primary hover:underline"
                >
                  Forgot Password?
                </a>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-lg bg-muted border border-border text-foreground font-semibold hover:bg-muted/80 transition-colors flex items-center justify-center gap-2"
            >
              <KeyRound className="w-4 h-4" />
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          <div className="pt-4 border-t border-border text-center text-xs text-muted-foreground">
            Don't have a customer account?{' '}
            <Link
              to="/register"
              className="font-semibold text-primary hover:underline"
            >
              Create Customer Account
            </Link>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  )
}
