import { Link } from '@tanstack/react-router'
import {
  BookOpen,
  Calendar,
  Car,
  ChevronDown,
  CreditCard,
  History,
  LayoutDashboard,
  MessageSquare,
  Moon,
  Sun,
  User,
  Users,
  Wrench,
} from 'lucide-react'
import * as React from 'react'

export default function Header() {
  const [theme, setTheme] = React.useState<'light' | 'dark'>('dark')
  const [isOpsOpen, setIsOpsOpen] = React.useState<boolean>(false)

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

          <Link
            to="/services"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <BookOpen className="w-4 h-4" />
            Services
          </Link>

          <Link
            to="/vehicles"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <Car className="w-4 h-4" />
            Vehicles
          </Link>

          <Link
            to="/appointments"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <Calendar className="w-4 h-4" />
            Appointments
          </Link>

          {/* Operations Dropdown */}
          <div
            role="none"
            className="relative"
            onMouseLeave={() => setIsOpsOpen(false)}
          >
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
                className="absolute left-0 mt-2 w-48 rounded-lg bg-card border border-border shadow-lg p-1 space-y-0.5 z-50"
              >
                <Link
                  to="/payments"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <CreditCard className="w-4 h-4 text-primary" />
                  Payments & Invoices
                </Link>

                <Link
                  to="/feedback"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <MessageSquare className="w-4 h-4 text-primary" />
                  Service Feedback
                </Link>

                <Link
                  to="/users"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <Users className="w-4 h-4 text-primary" />
                  User Accounts
                </Link>

                <Link
                  to="/audit-logs"
                  onClick={() => setIsOpsOpen(false)}
                  className="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                >
                  <History className="w-4 h-4 text-primary" />
                  Audit Logs
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

          <div className="flex items-center gap-2 px-2.5 py-1 rounded-full bg-muted border border-border text-xs font-semibold">
            <div className="w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold text-[10px]">
              <User className="w-3.5 h-3.5" />
            </div>
            <span className="hidden sm:inline">Admin</span>
          </div>
        </div>
      </div>
    </header>
  )
}
