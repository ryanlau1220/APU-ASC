import { Link } from '@tanstack/react-router'
import {
  BookOpen,
  Calendar,
  Car,
  CreditCard,
  FileCode,
  LayoutDashboard,
  MessageSquare,
  Moon,
  Sun,
  Wrench,
} from 'lucide-react'
import * as React from 'react'

export default function Header() {
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

        {/* Navigation Links */}
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

          <Link
            to="/payments"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <CreditCard className="w-4 h-4" />
            Payments
          </Link>

          <Link
            to="/feedback"
            activeProps={{
              className: 'text-primary bg-primary/10 border-primary/30',
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
          >
            <MessageSquare className="w-4 h-4" />
            Feedback
          </Link>
        </nav>

        {/* Actions Header */}
        <div className="flex items-center gap-3">
          <a
            href="http://localhost:8081/scalar"
            target="_blank"
            rel="noreferrer"
            className="hidden sm:flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1 rounded border border-border bg-muted hover:border-primary/40 transition-colors"
          >
            <FileCode className="w-3.5 h-3.5 text-primary" />
            Scalar Docs
          </a>

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
      </div>
    </header>
  )
}
