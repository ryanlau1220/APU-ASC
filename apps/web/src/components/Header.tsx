import { Link } from '@tanstack/react-router'
import {
  BookOpen,
  Calendar,
  Car,
  ChevronDown,
  CreditCard,
  FileText,
  LayoutDashboard,
  LogIn,
  LogOut,
  MessageSquare,
  Moon,
  ShieldCheck,
  Sun,
  User as UserIcon,
  Users,
  Wrench,
} from 'lucide-react'
import * as React from 'react'
import { useUserSession } from '../routes/__root'

export interface NavItem {
  label: string
  to: string
  icon: React.ComponentType<{ className?: string }>
}

const CUSTOMER_NAV: NavItem[] = [
  { label: 'Dashboard', to: '/customer/', icon: LayoutDashboard },
  { label: 'Vehicles', to: '/customer/vehicles', icon: Car },
  { label: 'Appointments', to: '/customer/appointments', icon: Calendar },
  { label: 'Quotations', to: '/customer/quotations', icon: FileText },
  { label: 'Payments', to: '/customer/payments', icon: CreditCard },
  { label: 'Feedback', to: '/customer/feedback', icon: MessageSquare },
]

const MANAGER_NAV: NavItem[] = [
  { label: 'Dashboard', to: '/manager/', icon: LayoutDashboard },
  { label: 'Services', to: '/manager/services', icon: BookOpen },
  { label: 'Appointments', to: '/manager/appointments', icon: Calendar },
  { label: 'Vehicles', to: '/manager/vehicles', icon: Car },
  { label: 'Observability', to: '/manager/audit-logs', icon: ShieldCheck },
  { label: 'Operations', to: '/manager/operations', icon: Wrench },
]

const STAFF_NAV: NavItem[] = [
  { label: 'Dashboard', to: '/staff/', icon: LayoutDashboard },
  { label: 'Services', to: '/staff/services', icon: BookOpen },
  { label: 'Appointments', to: '/staff/appointments', icon: Calendar },
  { label: 'Quotations', to: '/staff/quotations', icon: FileText },
  { label: 'Users', to: '/staff/users', icon: Users },
  { label: 'Payments', to: '/staff/payments', icon: CreditCard },
]

const TECHNICIAN_NAV: NavItem[] = [
  { label: 'Dashboard', to: '/technician/', icon: LayoutDashboard },
  { label: 'Appointments', to: '/technician/jobs', icon: Calendar },
  { label: 'Feedback', to: '/technician/feedback', icon: MessageSquare },
]

export function getRoleNavItems(
  roles: string[] = [],
  pathname?: string,
): NavItem[] {
  const isManager = roles.includes('MANAGER') || roles.includes('SYSTEM_ADMIN')

  // Only Managers/Admins can dynamically switch portal nav views
  if (isManager) {
    const currentPath =
      pathname ||
      (typeof window !== 'undefined' ? window.location.pathname : '')

    if (currentPath.startsWith('/customer')) {
      return CUSTOMER_NAV
    }
    if (currentPath.startsWith('/staff')) {
      return STAFF_NAV
    }
    if (currentPath.startsWith('/technician')) {
      return TECHNICIAN_NAV
    }
    if (currentPath.startsWith('/manager')) {
      return MANAGER_NAV
    }

    if (typeof window !== 'undefined') {
      const activeView = localStorage.getItem('active_portal_view')
      if (activeView === 'CUSTOMER') return CUSTOMER_NAV
      if (activeView === 'STAFF') return STAFF_NAV
      if (activeView === 'TECHNICIAN') return TECHNICIAN_NAV
      if (activeView === 'MANAGER') return MANAGER_NAV
    }

    return MANAGER_NAV
  }

  // Non-manager roles strictly receive their dedicated role nav
  if (roles.includes('STAFF') || roles.includes('ROLE_STAFF')) {
    return STAFF_NAV
  }
  if (roles.includes('TECHNICIAN') || roles.includes('ROLE_TECHNICIAN')) {
    return TECHNICIAN_NAV
  }
  return CUSTOMER_NAV
}

export function getRoleHomeRoute(
  roles: string[] = [],
  pathname?: string,
): string {
  const isManager = roles.includes('MANAGER') || roles.includes('SYSTEM_ADMIN')

  if (isManager) {
    const currentPath =
      pathname ||
      (typeof window !== 'undefined' ? window.location.pathname : '')

    if (currentPath.startsWith('/customer')) {
      return '/customer/'
    }
    if (currentPath.startsWith('/staff')) {
      return '/staff/'
    }
    if (currentPath.startsWith('/technician')) {
      return '/technician/'
    }
    if (currentPath.startsWith('/manager')) {
      return '/manager/'
    }

    if (typeof window !== 'undefined') {
      const activeView = localStorage.getItem('active_portal_view')
      if (activeView === 'CUSTOMER') return '/customer/'
      if (activeView === 'STAFF') return '/staff/'
      if (activeView === 'TECHNICIAN') return '/technician/'
      if (activeView === 'MANAGER') return '/manager/'
    }

    return '/manager/'
  }

  if (roles.includes('STAFF') || roles.includes('ROLE_STAFF')) {
    return '/staff/'
  }
  if (roles.includes('TECHNICIAN') || roles.includes('ROLE_TECHNICIAN')) {
    return '/technician/'
  }
  return '/customer/'
}

import { useTheme } from '../lib/useTheme'

export default function Header() {
  const { userSession } = useUserSession()
  const isAuthenticated = userSession?.authenticated ?? false
  const roles = userSession?.roles ?? []

  const { theme, toggleTheme } = useTheme()
  const [isUserMenuOpen, setIsUserMenuOpen] = React.useState<boolean>(false)

  const userMenuRef = React.useRef<HTMLDivElement>(null)

  // Click outside listener for dropdowns
  React.useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
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

  const handleLogout = () => {
    setIsUserMenuOpen(false)
    window.location.href = '/logout'
  }

  const handleSignIn = () => {
    window.location.href = '/oauth2/authorization/keycloak'
  }

  const homeRoute = getRoleHomeRoute(roles)
  const navItems = getRoleNavItems(roles)

  return (
    <header className="sticky top-0 z-50 bg-card/80 backdrop-blur-md border-b border-border transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo - Navigates to user's role-specific portal home */}
        <Link to={homeRoute as never} className="flex items-center gap-3">
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

        {/* Primary Role-Scoped Navigation Links */}
        {isAuthenticated && (
          <nav className="hidden md:flex items-center gap-1">
            {navItems.map((item) => {
              const Icon = item.icon
              const isDashboardLink = item.to.endsWith('/')

              return (
                <Link
                  key={item.to}
                  to={item.to as never}
                  activeOptions={isDashboardLink ? { exact: true } : undefined}
                  activeProps={{
                    className:
                      'text-primary bg-primary/10 border-primary/30 font-semibold',
                  }}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground transition-colors border border-transparent"
                >
                  <Icon className="w-4 h-4" />
                  {item.label}
                </Link>
              )
            })}
          </nav>
        )}

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
                className="p-1 rounded-full bg-muted border border-border hover:border-primary/40 transition-colors flex items-center gap-1"
                title={
                  userSession?.fullName ||
                  userSession?.username ||
                  'User Profile'
                }
              >
                <div className="w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold text-[10px] overflow-hidden">
                  {userSession?.avatarUrl ? (
                    <img
                      src={userSession.avatarUrl}
                      alt={userSession.fullName || 'User Avatar'}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <UserIcon className="w-4 h-4" />
                  )}
                </div>
                <ChevronDown className="w-3 h-3 text-muted-foreground mr-1" />
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
