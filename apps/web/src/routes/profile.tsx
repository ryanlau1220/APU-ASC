import { createFileRoute, Link } from '@tanstack/react-router'
import {
  Car,
  CheckCircle2,
  KeyRound,
  LayoutDashboard,
  Lock,
  Mail,
  Save,
  Shield,
  User as UserIcon,
  Users,
  Wrench,
} from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { bffFetch } from '../lib/apiClient'
import { useUserSession } from './__root'

export const Route = createFileRoute('/profile')({ component: ProfilePage })

function ProfilePage() {
  const { userSession } = useUserSession()
  const [fullName, setFullName] = React.useState(
    userSession?.fullName || 'System Administrator',
  )
  const [email, setEmail] = React.useState(
    userSession?.email || 'admin@apu-asc.com',
  )
  const [saved, setSaved] = React.useState(false)
  const [loading, setLoading] = React.useState(false)

  // Password Reset State
  const [oldPassword, setOldPassword] = React.useState('')
  const [newPassword, setNewPassword] = React.useState('')
  const [confirmPassword, setConfirmPassword] = React.useState('')
  const [passLoading, setPassLoading] = React.useState(false)
  const [passSuccess, setPassSuccess] = React.useState('')
  const [passError, setPassError] = React.useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    // Simulate profile update
    setTimeout(() => {
      setLoading(false)
      setSaved(true)
      setTimeout(() => setSaved(false), 3000)
    }, 500)
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setPassError('')
    setPassSuccess('')

    if (newPassword !== confirmPassword) {
      setPassError('New password and confirm password do not match.')
      return
    }

    if (newPassword.length < 8) {
      setPassError('New password must be at least 8 characters long.')
      return
    }

    setPassLoading(true)

    try {
      const res = await bffFetch<{ success: boolean; message: string }>(
        '/api/v1/auth/change-password',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            oldPassword,
            newPassword,
            confirmPassword,
          }),
        },
      )

      if (res?.success) {
        setPassSuccess(
          res.message ||
            'Password changed successfully. Redirecting to login...',
        )
        setOldPassword('')
        setNewPassword('')
        setConfirmPassword('')

        // Automatically log out user after password change (OWASP standard)
        setTimeout(() => {
          window.location.href = '/logout'
        }, 1500)
      } else {
        setPassError(
          res?.message ||
            'Failed to change password. Please check your current password.',
        )
      }
    } catch (err: unknown) {
      const errorMsg =
        err instanceof Error ? err.message : 'Current password is incorrect.'
      setPassError(errorMsg)
    } finally {
      setPassLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-4xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <UserIcon className="w-4 h-4" />
              Account Settings
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Edit Profile & Security
            </h1>
            <p className="text-xs text-muted-foreground">
              Update your personal details or change your account password.
            </p>
          </div>
        </section>

        {/* Profile Details Card */}
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6">
          <h2 className="font-heading text-lg font-bold border-b border-border pb-3 flex items-center gap-2">
            <UserIcon className="w-5 h-5 text-primary" />
            Profile Details
          </h2>

          {saved && (
            <div className="p-4 rounded-lg bg-status-completed/10 border border-status-completed/30 text-status-completed text-xs font-medium flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              Profile details saved successfully!
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5 text-xs">
            <div className="space-y-1.5">
              <label
                htmlFor="prof-fullname"
                className="font-semibold text-foreground block"
              >
                Full Name
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <UserIcon className="w-4 h-4" />
                </div>
                <input
                  id="prof-fullname"
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-muted border border-border focus:border-primary text-foreground outline-none transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="prof-email"
                className="font-semibold text-foreground block"
              >
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  id="prof-email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-muted border border-border focus:border-primary text-foreground outline-none transition-colors"
                />
              </div>
            </div>

            <div className="space-y-3">
              <span className="font-semibold text-foreground block">
                Assigned Role & Privileges
              </span>
              <div className="p-3 rounded-lg bg-muted border border-border flex items-center justify-between text-xs font-semibold text-foreground">
                <div className="flex items-center gap-2">
                  <Shield className="w-4 h-4 text-primary" />
                  <span>
                    {(userSession?.roles?.[0] || 'CUSTOMER').replace(
                      'ROLE_',
                      '',
                    )}
                  </span>
                </div>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-primary/10 text-primary border border-primary/30">
                  ACTIVE IDENTITY
                </span>
              </div>

              {(userSession?.roles?.includes('MANAGER') ||
                userSession?.roles?.includes('ROLE_MANAGER') ||
                userSession?.roles?.includes('WORKSHOP_MANAGER') ||
                userSession?.roles?.includes('SYSTEM_ADMIN')) && (
                <div className="p-4 rounded-xl border border-border bg-card space-y-3 mt-4">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-foreground flex items-center gap-2">
                      <LayoutDashboard className="w-4 h-4 text-primary" />
                      Executive Portal Switcher
                    </span>
                    <span className="text-[10px] text-muted-foreground font-medium">
                      Manager Multi-Portal Access
                    </span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    As a Manager, you have full access across all role-based
                    views. Click any portal below to switch view directly:
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                    <Link
                      to={'/manager/' as never}
                      className="p-3 rounded-lg border border-border hover:border-primary/50 bg-muted/40 hover:bg-muted transition-colors flex items-center gap-2.5 group"
                    >
                      <div className="w-7 h-7 rounded-md bg-primary/10 text-primary flex items-center justify-center font-bold text-xs">
                        <Shield className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="text-xs font-bold text-foreground group-hover:text-primary transition-colors">
                          Manager
                        </div>
                        <div className="text-[10px] text-muted-foreground">
                          Executive Suite
                        </div>
                      </div>
                    </Link>

                    <Link
                      to={'/staff/' as never}
                      className="p-3 rounded-lg border border-border hover:border-primary/50 bg-muted/40 hover:bg-muted transition-colors flex items-center gap-2.5 group"
                    >
                      <div className="w-7 h-7 rounded-md bg-primary/10 text-primary flex items-center justify-center font-bold text-xs">
                        <Users className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="text-xs font-bold text-foreground group-hover:text-primary transition-colors">
                          Staff
                        </div>
                        <div className="text-[10px] text-muted-foreground">
                          Counter Intake
                        </div>
                      </div>
                    </Link>

                    <Link
                      to={'/technician/' as never}
                      className="p-3 rounded-lg border border-border hover:border-primary/50 bg-muted/40 hover:bg-muted transition-colors flex items-center gap-2.5 group"
                    >
                      <div className="w-7 h-7 rounded-md bg-primary/10 text-primary flex items-center justify-center font-bold text-xs">
                        <Wrench className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="text-xs font-bold text-foreground group-hover:text-primary transition-colors">
                          Technician
                        </div>
                        <div className="text-[10px] text-muted-foreground">
                          Workshop Bays
                        </div>
                      </div>
                    </Link>

                    <Link
                      to={'/customer/' as never}
                      className="p-3 rounded-lg border border-border hover:border-primary/50 bg-muted/40 hover:bg-muted transition-colors flex items-center gap-2.5 group"
                    >
                      <div className="w-7 h-7 rounded-md bg-primary/10 text-primary flex items-center justify-center font-bold text-xs">
                        <Car className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="text-xs font-bold text-foreground group-hover:text-primary transition-colors">
                          Customer
                        </div>
                        <div className="text-[10px] text-muted-foreground">
                          Self Service
                        </div>
                      </div>
                    </Link>
                  </div>
                </div>
              )}
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="px-6 py-2.5 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center gap-2"
              >
                <Save className="w-4 h-4" />
                {loading ? 'Saving Changes...' : 'Save Profile Changes'}
              </button>
            </div>
          </form>
        </section>

        {/* Change Password Card */}
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6">
          <h2 className="font-heading text-lg font-bold border-b border-border pb-3 flex items-center gap-2">
            <KeyRound className="w-5 h-5 text-primary" />
            Security & Change Password
          </h2>

          {passError && (
            <div className="p-3.5 rounded-lg bg-destructive/10 border border-destructive/30 text-destructive text-xs font-medium">
              {passError}
            </div>
          )}

          {passSuccess && (
            <div className="p-3.5 rounded-lg bg-status-completed/10 border border-status-completed/30 text-status-completed text-xs font-medium flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              <span>{passSuccess}</span>
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-4 text-xs">
            <div className="space-y-1.5">
              <label
                htmlFor="old-password"
                className="font-semibold text-foreground block"
              >
                Current Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  id="old-password"
                  type="password"
                  required
                  value={oldPassword}
                  onChange={(e) => setOldPassword(e.target.value)}
                  placeholder="Enter your current password"
                  className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-background border border-input focus:border-primary text-foreground outline-none transition-colors"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label
                  htmlFor="new-password"
                  className="font-semibold text-foreground block"
                >
                  New Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    id="new-password"
                    type="password"
                    required
                    minLength={8}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="Minimum 8 characters"
                    className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-background border border-input focus:border-primary text-foreground outline-none transition-colors"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label
                  htmlFor="confirm-password"
                  className="font-semibold text-foreground block"
                >
                  Confirm New Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    id="confirm-password"
                    type="password"
                    required
                    minLength={8}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Re-enter new password"
                    className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-background border border-input focus:border-primary text-foreground outline-none transition-colors"
                  />
                </div>
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                disabled={passLoading}
                className="px-6 py-2.5 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center gap-2"
              >
                <KeyRound className="w-4 h-4" />
                {passLoading ? 'Updating Password...' : 'Update Password'}
              </button>
            </div>
          </form>
        </section>
      </main>

      <Footer />
    </div>
  )
}
