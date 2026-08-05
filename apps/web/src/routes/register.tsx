import { createFileRoute, Link, useNavigate } from '@tanstack/react-router'
import {
  CheckCircle2,
  Lock,
  Mail,
  Moon,
  Sun,
  User,
  UserPlus,
  Wrench,
} from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'

export const Route = createFileRoute('/register')({
  head: () => ({
    meta: [{ title: 'Register Account | APU Automotive Service Centre' }],
  }),
  component: RegisterPage,
})

function RegisterPage() {
  const navigate = useNavigate()
  const [fullName, setFullName] = React.useState('')
  const [email, setEmail] = React.useState('')
  const [password, setPassword] = React.useState('')
  const [submitted, setSubmitted] = React.useState(false)
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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    // Simulate customer registration request
    setTimeout(() => {
      setLoading(false)
      setSubmitted(true)
    }, 600)
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
            aria-label={
              theme === 'dark'
                ? 'Switch to light theme'
                : 'Switch to dark theme'
            }
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
              <UserPlus className="w-6 h-6" />
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Customer Self-Registration
            </h1>
            <p className="text-xs text-muted-foreground">
              Create an account to book vehicle services, track repair status,
              and view invoices.
            </p>
          </div>

          {submitted ? (
            <output className="block p-5 rounded-xl bg-status-completed/10 border border-status-completed/30 space-y-3 text-center text-xs">
              <CheckCircle2 className="w-8 h-8 text-status-completed mx-auto" />
              <h3 className="font-heading font-bold text-sm text-foreground">
                Verification Email Sent!
              </h3>
              <p className="text-muted-foreground leading-relaxed">
                We have sent an activation link to{' '}
                <strong className="text-foreground">{email}</strong>. Please
                check your inbox and verify your email before signing in.
              </p>
              <button
                type="button"
                onClick={() => navigate({ to: '/login' })}
                className="mt-2 w-full py-2 rounded-lg bg-primary text-primary-foreground font-semibold"
              >
                Go to Sign In
              </button>
            </output>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4 text-xs">
              <div className="space-y-1.5">
                <label
                  htmlFor="reg-fullname"
                  className="font-semibold text-foreground block"
                >
                  Full Name
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    id="reg-fullname"
                    type="text"
                    name="fullName"
                    required
                    placeholder="Ryan Lau"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    autoComplete="name"
                    className="w-full pl-9 pr-3 py-2 rounded-lg bg-muted border border-border focus:border-primary text-foreground placeholder:text-muted-foreground outline-none transition-colors"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label
                  htmlFor="reg-email"
                  className="font-semibold text-foreground block"
                >
                  Email Address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    id="reg-email"
                    type="email"
                    name="email"
                    required
                    placeholder="customer@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                    spellCheck={false}
                    className="w-full pl-9 pr-3 py-2 rounded-lg bg-muted border border-border focus:border-primary text-foreground placeholder:text-muted-foreground outline-none transition-colors"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label
                  htmlFor="reg-password"
                  className="font-semibold text-foreground block"
                >
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    id="reg-password"
                    type="password"
                    name="password"
                    required
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    className="w-full pl-9 pr-3 py-2 rounded-lg bg-muted border border-border focus:border-primary text-foreground placeholder:text-muted-foreground outline-none transition-colors"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
              >
                <UserPlus className="w-4 h-4" />
                {loading ? 'Creating Account…' : 'Register Customer Account'}
              </button>
            </form>
          )}

          <div className="pt-4 border-t border-border text-center text-xs text-muted-foreground">
            Already registered?{' '}
            <Link
              to="/login"
              className="font-semibold text-primary hover:underline"
            >
              Sign In Here
            </Link>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  )
}
