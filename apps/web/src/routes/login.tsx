import { createFileRoute, Link } from '@tanstack/react-router'
import { LogIn, Moon, ShieldCheck, Sun, Wrench } from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'

export const Route = createFileRoute('/login')({
  head: () => ({
    meta: [{ title: 'Sign In | APU Automotive Service Centre' }],
  }),
  component: LoginPage,
})

function LoginPage() {
  const [theme, setTheme] = React.useState<'light' | 'dark'>('dark')
  const [loggedOut, setLoggedOut] = React.useState(false)

  React.useEffect(() => {
    const root = document.documentElement
    const isDark =
      root.classList.contains('dark') ||
      root.getAttribute('data-theme') === 'dark'
    setTheme(isDark ? 'dark' : 'light')

    if (new URLSearchParams(window.location.search).has('loggedOut')) {
      setLoggedOut(true)
      return
    }

    // Automatically initiate Keycloak Enterprise SSO redirect.
    const timer = setTimeout(() => {
      window.location.href = '/oauth2/authorization/keycloak'
    }, 400)
    return () => clearTimeout(timer)
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
        <div className="w-full max-w-md bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6 text-center">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 border border-primary/30 text-primary mb-2 animate-bounce">
            <LogIn className="w-6 h-6" />
          </div>
          <h1 className="font-heading text-2xl font-bold">
            {loggedOut ? 'You have signed out' : 'Redirecting to APU-ASC SSO'}
          </h1>
          <p className="text-xs text-muted-foreground">
            {loggedOut
              ? 'Sign in again when you are ready.'
              : 'Connecting securely to APU Automotive Service Centre Enterprise Identity Service…'}
          </p>

          <a
            href="/oauth2/authorization/keycloak"
            className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center justify-center gap-2 text-xs shadow-md"
          >
            <ShieldCheck className="w-4 h-4" />
            Proceed to Secure SSO Sign In
          </a>
        </div>
      </main>

      <Footer />
    </div>
  )
}
