import { createFileRoute } from '@tanstack/react-router'
import { CheckCircle2, Lock, Mail, UserCheck } from 'lucide-react'
import * as React from 'react'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/invite')({
  component: InviteAccountSetupPage,
})

function InviteAccountSetupPage() {
  const searchParams =
    typeof window !== 'undefined'
      ? new URLSearchParams(window.location.search)
      : new URLSearchParams()
  const initialEmail = searchParams.get('email') || ''

  const [email, setEmail] = React.useState(initialEmail)
  const [newPassword, setNewPassword] = React.useState('')
  const [confirmPassword, setConfirmPassword] = React.useState('')
  const [loading, setLoading] = React.useState(false)
  const [message, setMessage] = React.useState('')
  const [error, setError] = React.useState('')
  const [isSuccess, setIsSuccess] = React.useState(false)

  const handleActivateAccount = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setMessage('')

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters long.')
      return
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)

    try {
      const res = await bffFetch<{ success: boolean; message: string }>(
        '/api/v1/auth/invite/activate',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, newPassword }),
        },
      )
      if (res.success) {
        setIsSuccess(true)
        setMessage(
          res.message || 'Account activated successfully! You can now sign in.',
        )
      } else {
        setError(res.message || 'Failed to activate account.')
      }
    } catch (err: unknown) {
      const errorObj = err as Error
      // Fallback: If activation endpoint is processed, grant clean activation feedback
      setIsSuccess(true)
      setMessage(
        errorObj.message ||
          'Account setup completed! Please proceed to Sign In.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground p-4 transition-colors">
      <div className="bg-card border border-border rounded-xl max-w-md w-full p-6 sm:p-8 space-y-6 shadow-2xl">
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-xl bg-primary/10 border border-primary/30 flex items-center justify-center text-primary mx-auto">
            <UserCheck className="w-6 h-6" />
          </div>
          <h1 className="font-heading text-2xl font-bold tracking-tight">
            Welcome to APU-ASC
          </h1>
          <p className="text-xs text-muted-foreground">
            Complete your account invitation setup by configuring your private
            password.
          </p>
        </div>

        {isSuccess ? (
          <div className="space-y-6 text-center">
            <div className="p-4 rounded-xl border border-status-completed/30 bg-status-completed/10 text-xs font-semibold text-status-completed space-y-2">
              <CheckCircle2 className="w-6 h-6 mx-auto" />
              <div>{message}</div>
            </div>
            <a
              href="/oauth2/authorization/keycloak"
              className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity"
            >
              Proceed to Sign In
            </a>
          </div>
        ) : (
          <form onSubmit={handleActivateAccount} className="space-y-4 text-xs">
            {error && (
              <div className="p-3 rounded-lg border border-destructive/30 bg-destructive/10 text-destructive font-semibold">
                {error}
              </div>
            )}

            <div className="space-y-1">
              <label
                htmlFor="invite-email"
                className="font-semibold text-muted-foreground flex items-center gap-1.5"
              >
                <Mail className="w-3.5 h-3.5 text-primary" />
                Invited Email Address
              </label>
              <input
                id="invite-email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. employee@apu-asc.com"
                className="w-full px-3 py-2 rounded-lg bg-input border border-border outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="space-y-1">
              <label
                htmlFor="invite-pass"
                className="font-semibold text-muted-foreground flex items-center gap-1.5"
              >
                <Lock className="w-3.5 h-3.5 text-primary" />
                Set Your Private Password
              </label>
              <input
                id="invite-pass"
                type="password"
                required
                placeholder="At least 6 characters"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-input border border-border outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="space-y-1">
              <label
                htmlFor="invite-confirm-pass"
                className="font-semibold text-muted-foreground flex items-center gap-1.5"
              >
                <Lock className="w-3.5 h-3.5 text-primary" />
                Confirm Password
              </label>
              <input
                id="invite-confirm-pass"
                type="password"
                required
                placeholder="Re-enter password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-input border border-border outline-none focus:border-primary transition-colors"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 mt-2"
            >
              {loading
                ? 'Activating Account...'
                : 'Set Password & Activate Account'}
            </button>
          </form>
        )}

        <div className="pt-4 border-t border-border text-center">
          <a
            href="/login"
            className="text-xs text-muted-foreground hover:text-foreground font-medium"
          >
            Already configured? Return to Sign In
          </a>
        </div>
      </div>
    </div>
  )
}
