import { Link } from '@tanstack/react-router'
import {
  BookOpen,
  Calendar,
  Car,
  ChevronDown,
  CreditCard,
  LayoutDashboard,
  LogIn,
  LogOut,
  MessageSquare,
  Moon,
  Sun,
  User as UserIcon,
  Users,
  Wrench,
} from 'lucide-react'
import * as React from 'react'
import { useUserSession } from '../routes/__root'

export default function Header() {
  const { userSession } = useUserSession()
  const isAuthenticated = userSession?.authenticated ?? false

  const [theme, setTheme] = React.useState<'light' | 'dark'>('dark')
  const [isOpsOpen, setIsOpsOpen] = React.useState<boolean>(false)
  const [isUserMenuOpen, setIsUserMenuOpen] = React.useState<boolean>(false)

  const opsRef = React.useRef<HTMLDivElement>(null)
  const userMenuRef = React.useRef<HTMLDivElement>(null)

  React.useEffect(() => {
    const root = document.documentElement
    const isDark =
      root.classList.contains('dark') ||
      root.getAttribute('data-theme') === 'dark'
    setTheme(isDark ? 'dark' : 'light')
  }, [])

  // Click outside listener for dropdowns
  React.useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (opsRef.current && !opsRef.current.contains(event.target as Node)) {
        setIsOpsOpen(false)
      }
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target as Node)
      ) {
        setIsUserMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
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

  const handleLogout = () => {
    setIsUserMenuOpen(false)
    window.location.href = '/logout'
  }

  const handleSignIn = () => {
    window.location.href = '/oauth2/authorization/keycloak'
  }

  return (
    <header className="sticky top-0 z-50 bg-card/80 backdrop-blur-md border-b border-border transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-primary/10 border border-primary/30 flex items-center justify-center text-primary font-bold text-xl">
            <Wrench className="w-5 h-5" />
          </div>
          <div>
            <span className="font-heading font-bold text-lg tracking-tight block leading-none">
              APU-ASC
            </span>
            <span className="text-[10px] text-muted-foreground tracking-widest uppercase block mt-1">
              Automotive Service Centre
            </span>
          </div>
        </Link>

        {/* Primary Navigation Links */}
        <nav className="hidden md:flex items-center gap-1">
          <Link
            to="/"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <LayoutDashboard className="w-4 h-4" />
            Dashboard
          </Link>

          {/* Role-based navigation items */}
          <Link
            to="/staff/services"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <BookOpen className="w-4 h-4" />
            Services
          </Link>

          <Link
            to="/customer/vehicles"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <Car className="w-4 h-4" />
            Vehicles
          </Link>

          <Link
            to="/customer/appointments"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <Calendar className="w-4 h-4" />
            Appointments
          </Link>

          {/* Operations Dropdown */}
          <div ref={opsRef} className="relative">
            <button
              type="button"
              onClick={() => setIsOpsOpen(!isOpsOpen)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
            >
              Operations
              <ChevronDown
                className={`w-3.5 h-3.5 transition-transform ${isOpsOpen ? 'rotate-180' : ''}`}
              />
            </button>

            {isOpsOpen && (
              <div
                role="menu"
                className="absolute left-0 mt-2 w-52 rounded-xl bg-card opacity-100 shadow-2xl border border-border p-1.5 space-y-1 z-50 font-sans"
              >
                <Link
                  to="/customer/payments"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <CreditCard className="w-4 h-4 text-primary" />
                  Payments & Invoices
                </Link>

                <Link
                  to="/customer/feedback"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <MessageSquare className="w-4 h-4 text-primary" />
                  Service Feedback
                </Link>

                <Link
                  to="/manager/operations"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <Users className="w-4 h-4 text-primary" />
                  Manager Suite
                </Link>
              </div>
            )}
          </div>
        </nav>

        {/* Actions Header */}
        <div className="flex items-center gap-3">
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

          {/* User Menu / Sign In */}
          {isAuthenticated ? (
            <div ref={userMenuRef} className="relative">
              <button
                type="button"
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 px-2.5 py-1 rounded-full bg-muted border border-border text-xs font-semibold hover:border-primary/40 transition-colors"
              >
                <div className="w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold text-[10px]">
                  <UserIcon className="w-3.5 h-3.5" />
                </div>
                <span className="hidden sm:inline">
                  {userSession?.fullName || userSession?.username || 'User'}
                </span>
                <ChevronDown className="w-3 h-3 text-muted-foreground" />
              </button>

              {isUserMenuOpen && (
                <div
                  role="menu"
                  className="absolute right-0 mt-2 w-52 rounded-xl bg-card opacity-100 shadow-2xl border border-border p-1.5 space-y-1 z-50 font-sans"
                >
                  <div className="px-3 py-2 border-b border-border text-xs">
                    <div className="font-bold text-foreground">
                      {userSession?.fullName || userSession?.username}
                    </div>
                    <div className="text-[10px] text-muted-foreground">
                      {userSession?.email || 'authenticated'}
                    </div>
                  </div>

                  <Link
                    to="/profile"
                    onClick={() => setIsUserMenuOpen(false)}
                    className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                  >
                    <UserIcon className="w-4 h-4 text-primary" />
                    Edit Profile
                  </Link>

                  <button
                    type="button"
                    onClick={handleLogout}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-status-pending hover:bg-status-pending/10 transition-colors text-left"
                  >
                    <LogOut className="w-4 h-4" />
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              type="button"
              onClick={handleSignIn}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity"
            >
              <LogIn className="w-3.5 h-3.5" />
              Sign In
            </button>
          )}
        </div>
      </div>
    </header>
  )
}
