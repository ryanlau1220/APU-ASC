import { createFileRoute } from '@tanstack/react-router'
import {
  ArrowLeft,
  CheckCircle2,
  KeyRound,
  Lock,
  Mail,
  ShieldCheck,
} from 'lucide-react'
import * as React from 'react'
import {
  useRequestForgotPasswordOtp,
  useResetPasswordWithOtp,
} from '../api/generated/endpoints'

export const Route = createFileRoute('/forgot-password')({
  component: ForgotPasswordPage,
})

function ForgotPasswordPage() {
  const [step, setStep] = React.useState<'request' | 'reset'>('request')
  const [email, setEmail] = React.useState('')
  const [otp, setOtp] = React.useState('')
  const [newPassword, setNewPassword] = React.useState('')
  const [confirmPassword, setConfirmPassword] = React.useState('')
  const [loading, setLoading] = React.useState(false)
  const [message, setMessage] = React.useState('')
  const [error, setError] = React.useState('')
  const [isSuccess, setIsSuccess] = React.useState(false)
  const requestOtpMutation = useRequestForgotPasswordOtp<Error>()
  const resetPasswordMutation = useResetPasswordWithOtp<Error>()

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setMessage('')
    setLoading(true)

    try {
      const res = (await requestOtpMutation.mutateAsync({
        data: { email },
      })) as { success?: boolean; message?: string }

      if (res?.success) {
        setMessage(res.message || 'OTP code sent. Please check your email.')
        setStep('reset')
      } else {
        setError(res?.message || 'Failed to send OTP code. Please try again.')
      }
    } catch (err: unknown) {
      const errorMsg =
        err instanceof Error
          ? err.message
          : 'Failed to send OTP. Please check your connection.'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setMessage('')

    if (newPassword !== confirmPassword) {
      setError('New password and confirm password do not match.')
      return
    }

    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters long.')
      return
    }

    setLoading(true)

    try {
      const res = (await resetPasswordMutation.mutateAsync({
        data: { email, otp, newPassword, confirmPassword },
      })) as { success?: boolean; message?: string }

      if (res?.success) {
        setMessage(res.message || 'Password reset successfully.')
        setIsSuccess(true)
      } else {
        setError(res?.message || 'Failed to reset password.')
      }
    } catch (err: unknown) {
      const errorMsg =
        err instanceof Error
          ? err.message
          : 'Failed to reset password. Check your OTP code.'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background text-foreground transition-colors p-4 sm:p-6">
      <main className="max-w-md w-full mx-auto">
        <div className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6 shadow-lg">
          <div className="text-center space-y-2">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 text-primary mb-2">
              <KeyRound className="w-6 h-6" />
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Forgot Password OTP
            </h1>
            <p className="text-xs text-muted-foreground">
              {step === 'request'
                ? 'Enter your registered email address to receive a 6-digit verification code.'
                : `Enter the 6-digit OTP sent to ${email} and your new password.`}
            </p>
          </div>

          {error && (
            <div className="p-3.5 rounded-lg bg-destructive/10 border border-destructive/30 text-destructive text-xs font-medium">
              {error}
            </div>
          )}

          {message && (
            <div className="p-3.5 rounded-lg bg-status-completed/10 border border-status-completed/30 text-status-completed text-xs font-medium flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
              <span>{message}</span>
            </div>
          )}

          {isSuccess ? (
            <div className="space-y-4 pt-2 text-center">
              <a
                href="/oauth2/authorization/keycloak"
                className="w-full inline-flex justify-center items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground font-medium py-2.5 px-4 rounded-lg text-xs transition-colors shadow-md"
              >
                Proceed to Sign In
              </a>
            </div>
          ) : step === 'request' ? (
            <form onSubmit={handleRequestOtp} className="space-y-4 text-xs">
              <div className="space-y-1.5">
                <label
                  htmlFor="forgot-email"
                  className="font-semibold text-foreground block"
                >
                  Email Address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    id="forgot-email"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="user@example.com"
                    className="w-full pl-9 pr-3 py-2 bg-background border border-input rounded-lg focus:outline-none focus:ring-2 focus:ring-ring text-foreground text-xs"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-primary hover:bg-primary/90 disabled:opacity-50 text-primary-foreground font-semibold py-2.5 px-4 rounded-lg transition-colors shadow-sm flex items-center justify-center gap-2"
              >
                {loading ? 'Sending Code...' : 'Send OTP Verification Code'}
              </button>

              <div className="pt-2 text-center">
                <a
                  href="/oauth2/authorization/keycloak"
                  className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  Back to Sign In
                </a>
              </div>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className="space-y-4 text-xs">
              <div className="space-y-1.5">
                <label
                  htmlFor="forgot-otp"
                  className="font-semibold text-foreground block"
                >
                  6-Digit OTP Code
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <input
                    id="forgot-otp"
                    type="text"
                    required
                    maxLength={6}
                    value={otp}
                    onChange={(e) =>
                      setOtp(e.target.value.replace(/[^0-9]/g, ''))
                    }
                    placeholder="123456"
                    className="w-full pl-9 pr-3 py-2 bg-background border border-input rounded-lg focus:outline-none focus:ring-2 focus:ring-ring text-foreground font-mono text-center tracking-widest text-sm"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label
                  htmlFor="forgot-new-pass"
                  className="font-semibold text-foreground block"
                >
                  New Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    id="forgot-new-pass"
                    type="password"
                    required
                    minLength={8}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="Minimum 8 characters"
                    className="w-full pl-9 pr-3 py-2 bg-background border border-input rounded-lg focus:outline-none focus:ring-2 focus:ring-ring text-foreground text-xs"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label
                  htmlFor="forgot-confirm-pass"
                  className="font-semibold text-foreground block"
                >
                  Confirm New Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    id="forgot-confirm-pass"
                    type="password"
                    required
                    minLength={8}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Re-enter new password"
                    className="w-full pl-9 pr-3 py-2 bg-background border border-input rounded-lg focus:outline-none focus:ring-2 focus:ring-ring text-foreground text-xs"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-primary hover:bg-primary/90 disabled:opacity-50 text-primary-foreground font-semibold py-2.5 px-4 rounded-lg transition-colors shadow-sm flex items-center justify-center gap-2"
              >
                {loading ? 'Resetting Password...' : 'Reset Password'}
              </button>

              <div className="pt-2 flex items-center justify-between text-xs text-muted-foreground">
                <button
                  type="button"
                  onClick={() => setStep('request')}
                  className="hover:text-foreground transition-colors"
                >
                  Resend OTP Code
                </button>
                <a
                  href="/oauth2/authorization/keycloak"
                  className="inline-flex items-center gap-1 hover:text-foreground transition-colors"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  Back to Sign In
                </a>
              </div>
            </form>
          )}
        </div>
      </main>
    </div>
  )
}
