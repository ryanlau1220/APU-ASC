import { createFileRoute } from '@tanstack/react-router'
import {
  BellRing,
  Car,
  CheckCircle2,
  Globe2,
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
import {
  useChangePassword,
  useGetMyPreferences,
  useUpdateMyPreferences,
  useUpdateUser,
  useUploadAvatar,
} from '../api/generated/endpoints'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { useUserSession } from './__root'

const TIME_ZONES = [
  ['Asia/Kuala_Lumpur', 'Kuala Lumpur (UTC+8)'],
  ['Asia/Singapore', 'Singapore (UTC+8)'],
  ['Asia/Bangkok', 'Bangkok (UTC+7)'],
  ['Asia/Jakarta', 'Jakarta (UTC+7)'],
  ['Asia/Manila', 'Manila (UTC+8)'],
  ['UTC', 'UTC'],
] as const

export const Route = createFileRoute('/profile')({
  head: () => ({
    meta: [
      { title: 'User Profile & Settings | APU Automotive Service Centre' },
    ],
  }),
  component: ProfilePage,
})

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

  // Active Portal View Selection for Managers
  const [selectedPortal, setSelectedPortal] = React.useState<string>(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('active_portal_view')
      if (stored) return stored
      const ref = document.referrer || ''
      if (ref.includes('/customer')) return 'CUSTOMER'
      if (ref.includes('/staff')) return 'STAFF'
      if (ref.includes('/technician')) return 'TECHNICIAN'
      if (ref.includes('/manager')) return 'MANAGER'
    }
    return 'MANAGER'
  })

  // Password Reset State
  const [oldPassword, setOldPassword] = React.useState('')
  const [newPassword, setNewPassword] = React.useState('')
  const [confirmPassword, setConfirmPassword] = React.useState('')
  const [passLoading, setPassLoading] = React.useState(false)
  const [passSuccess, setPassSuccess] = React.useState('')
  const [passError, setPassError] = React.useState('')

  // Avatar Upload State
  const [avatarFile, setAvatarFile] = React.useState<File | null>(null)
  const [avatarPreview, setAvatarPreview] = React.useState<string>('')
  const [avatarUploading, setAvatarUploading] = React.useState(false)
  const [avatarSuccess, setAvatarSuccess] = React.useState('')

  const [timeZone, setTimeZone] = React.useState('Asia/Kuala_Lumpur')
  const [inAppNotificationsEnabled, setInAppNotificationsEnabled] =
    React.useState(true)
  const [preferencesSaved, setPreferencesSaved] = React.useState(false)
  const [preferencesError, setPreferencesError] = React.useState('')

  const uploadAvatarMutation = useUploadAvatar<Error>()
  const updateUserMutation = useUpdateUser<Error>()
  const changePasswordMutation = useChangePassword<Error>()
  const { data: preferences } = useGetMyPreferences()
  const updatePreferencesMutation = useUpdateMyPreferences<Error>()

  React.useEffect(() => {
    if (userSession?.fullName) setFullName(userSession.fullName)
    if (userSession?.email) setEmail(userSession.email)
  }, [userSession])

  React.useEffect(() => {
    if (!preferences) return
    if (preferences.timeZone) setTimeZone(preferences.timeZone)
    if (typeof preferences.inAppNotificationsEnabled === 'boolean') {
      setInAppNotificationsEnabled(preferences.inAppNotificationsEnabled)
    }
  }, [preferences])

  const handleAvatarUpload = async () => {
    if (!avatarFile) return
    setAvatarUploading(true)
    setAvatarSuccess('')

    try {
      await uploadAvatarMutation.mutateAsync({
        data: { file: avatarFile },
      })
      setAvatarSuccess('Avatar picture updated! Reloading session...')
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } catch (err) {
      console.error('Avatar upload failed:', err)
    } finally {
      setAvatarUploading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    if (typeof window !== 'undefined') {
      localStorage.setItem('active_portal_view', selectedPortal)
    }

    try {
      if (userSession?.id) {
        await updateUserMutation.mutateAsync({
          id: userSession.id,
          data: { fullName, email },
        })
      }
      setSaved(true)

      const portalRoutes: Record<string, string> = {
        MANAGER: '/manager/',
        STAFF: '/staff/',
        TECHNICIAN: '/technician/',
        CUSTOMER: '/customer/',
      }
      const targetRoute = portalRoutes[selectedPortal] || '/manager/'

      setTimeout(() => {
        window.location.href = targetRoute
      }, 1000)
    } catch (err) {
      console.error('Failed to save profile changes:', err)
    } finally {
      setLoading(false)
    }
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
      const res = (await changePasswordMutation.mutateAsync({
        data: { oldPassword, newPassword, confirmPassword },
      })) as { success?: boolean; message?: string }

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

  const handlePreferencesSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setPreferencesSaved(false)
    setPreferencesError('')
    try {
      await updatePreferencesMutation.mutateAsync({
        data: { timeZone, inAppNotificationsEnabled },
      })
      setPreferencesSaved(true)
    } catch (error) {
      setPreferencesError(
        error instanceof Error ? error.message : 'Unable to save preferences.',
      )
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

          {/* Avatar Upload Section */}
          <div className="flex flex-col sm:flex-row items-center gap-6 p-4 rounded-xl bg-muted/40 border border-border">
            <div className="relative group w-20 h-20 rounded-full bg-primary/10 border-2 border-primary/40 flex items-center justify-center overflow-hidden shrink-0 shadow-md">
              {avatarPreview || userSession?.avatarUrl ? (
                <img
                  src={avatarPreview || userSession?.avatarUrl}
                  alt="Profile Avatar"
                  className="w-full h-full object-cover"
                />
              ) : (
                <UserIcon className="w-9 h-9 text-primary" />
              )}
            </div>
            <div className="space-y-2 text-center sm:text-left flex-1">
              <div className="font-semibold text-sm text-foreground">
                Profile Picture
              </div>
              <p className="text-xs text-muted-foreground">
                Upload a JPEG or PNG image. Stored securely in MinIO Object
                Storage.
              </p>
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-3 pt-1">
                <label className="cursor-pointer inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:bg-primary/90 transition-colors shadow-sm">
                  <UserIcon className="w-3.5 h-3.5" />
                  Choose File
                  <input
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={(e) => {
                      const file = e.target.files?.[0]
                      if (file) {
                        setAvatarFile(file)
                        setAvatarPreview(URL.createObjectURL(file))
                      }
                    }}
                  />
                </label>
                {avatarFile && (
                  <button
                    type="button"
                    onClick={handleAvatarUpload}
                    disabled={avatarUploading}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-status-completed text-white text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
                  >
                    {avatarUploading ? 'Uploading...' : 'Save Picture'}
                  </button>
                )}
              </div>
              {avatarSuccess && (
                <div className="text-xs font-medium text-status-completed flex items-center gap-1 mt-1">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  {avatarSuccess}
                </div>
              )}
            </div>
          </div>

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
                    As a Manager, select your target portal view below and click{' '}
                    <strong>Save Profile Changes</strong> to switch view
                    directly:
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                    {[
                      {
                        key: 'MANAGER',
                        label: 'Manager',
                        subtext: 'Executive Suite',
                        icon: Shield,
                        route: '/manager/',
                      },
                      {
                        key: 'STAFF',
                        label: 'Staff',
                        subtext: 'Counter Intake',
                        icon: Users,
                        route: '/staff/',
                      },
                      {
                        key: 'TECHNICIAN',
                        label: 'Technician',
                        subtext: 'Workshop Bays',
                        icon: Wrench,
                        route: '/technician/',
                      },
                      {
                        key: 'CUSTOMER',
                        label: 'Customer',
                        subtext: 'Self Service',
                        icon: Car,
                        route: '/customer/',
                      },
                    ].map((portal) => {
                      const isSelected = selectedPortal === portal.key
                      const IconComp = portal.icon
                      return (
                        <button
                          key={portal.key}
                          type="button"
                          onClick={() => setSelectedPortal(portal.key)}
                          className={`p-3 rounded-lg border text-left transition-all relative flex items-center gap-2.5 ${
                            isSelected
                              ? 'border-primary bg-primary/10 ring-1 ring-primary/40 shadow-sm'
                              : 'border-border bg-muted/30 hover:border-primary/40 hover:bg-muted/70'
                          }`}
                        >
                          <div
                            className={`w-7 h-7 rounded-md flex items-center justify-center font-bold text-xs ${
                              isSelected
                                ? 'bg-primary text-primary-foreground'
                                : 'bg-muted text-muted-foreground'
                            }`}
                          >
                            <IconComp className="w-4 h-4" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between">
                              <span
                                className={`text-xs font-bold ${
                                  isSelected
                                    ? 'text-primary'
                                    : 'text-foreground'
                                }`}
                              >
                                {portal.label}
                              </span>
                              {isSelected && (
                                <CheckCircle2 className="w-3.5 h-3.5 text-primary" />
                              )}
                            </div>
                            <div className="text-[10px] text-muted-foreground truncate">
                              {portal.subtext}
                            </div>
                          </div>
                        </button>
                      )
                    })}
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

        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6">
          <div className="border-b border-border pb-3">
            <h2 className="font-heading text-lg font-bold flex items-center gap-2">
              <BellRing className="w-5 h-5 text-primary" />
              Preferences
            </h2>
            <p className="mt-1 text-xs text-muted-foreground">
              Use your local time zone for notification timestamps and control
              in-app alerts.
            </p>
          </div>

          {preferencesSaved && (
            <div className="p-3.5 rounded-lg bg-status-completed/10 border border-status-completed/30 text-status-completed text-xs font-medium flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              Preferences saved.
            </div>
          )}
          {preferencesError && (
            <div className="p-3.5 rounded-lg bg-destructive/10 border border-destructive/30 text-destructive text-xs font-medium">
              {preferencesError}
            </div>
          )}

          <form
            onSubmit={handlePreferencesSubmit}
            className="space-y-5 text-xs"
          >
            <div className="space-y-1.5">
              <label
                htmlFor="time-zone"
                className="font-semibold text-foreground block"
              >
                Time Zone
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                  <Globe2 className="w-4 h-4" />
                </div>
                <select
                  id="time-zone"
                  value={timeZone}
                  onChange={(e) => setTimeZone(e.target.value)}
                  className="w-full appearance-none pl-9 pr-3 py-2.5 rounded-lg bg-muted border border-border focus:border-primary text-foreground outline-none transition-colors"
                >
                  {TIME_ZONES.map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <label className="flex items-start gap-3 rounded-xl border border-border bg-muted/40 p-4 cursor-pointer">
              <input
                type="checkbox"
                checked={inAppNotificationsEnabled}
                onChange={(e) => setInAppNotificationsEnabled(e.target.checked)}
                className="mt-0.5 h-4 w-4 accent-primary"
              />
              <span className="space-y-1">
                <span className="block font-semibold text-foreground">
                  In-app notifications
                </span>
                <span className="block text-muted-foreground">
                  Receive appointment, quotation, and work-order updates in the
                  portal.
                </span>
              </span>
            </label>

            <div className="pt-1 flex justify-end">
              <button
                type="submit"
                disabled={updatePreferencesMutation.isPending}
                className="px-6 py-2.5 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center gap-2 disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                {updatePreferencesMutation.isPending
                  ? 'Saving Preferences...'
                  : 'Save Preferences'}
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
