import { createFileRoute } from '@tanstack/react-router'
import {
  CheckCircle2,
  Mail,
  Phone,
  Save,
  Shield,
  User as UserIcon,
} from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/profile')({ component: ProfilePage })

function ProfilePage() {
  const [fullName, setFullName] = React.useState('System Administrator')
  const [contactNumber, setContactNumber] = React.useState('+60123456789')
  const [email, setEmail] = React.useState('admin@apu-asc.com')
  const [saved, setSaved] = React.useState(false)
  const [loading, setLoading] = React.useState(false)

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
              Edit Profile Details
            </h1>
            <p className="text-xs text-muted-foreground">
              Update your personal full name, contact number, and registered
              email address.
            </p>
          </div>
        </section>

        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6">
          {saved && (
            <div className="p-4 rounded-lg bg-status-completed/10 border border-status-completed/30 text-status-completed text-xs font-medium flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              Profile details saved successfully!
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5 text-xs">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
                  htmlFor="prof-contact"
                  className="font-semibold text-foreground block"
                >
                  Contact Number
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-muted-foreground">
                    <Phone className="w-4 h-4" />
                  </div>
                  <input
                    id="prof-contact"
                    type="text"
                    required
                    value={contactNumber}
                    onChange={(e) => setContactNumber(e.target.value)}
                    className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-muted border border-border focus:border-primary text-foreground outline-none transition-colors"
                  />
                </div>
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

            <div className="space-y-1.5">
              <span className="font-semibold text-foreground block">
                Assigned Role & Privileges
              </span>
              <div className="p-3 rounded-lg bg-muted border border-border flex items-center gap-2 text-xs font-semibold text-foreground">
                <Shield className="w-4 h-4 text-primary" />
                MANAGER (Full System Administrator)
              </div>
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
      </main>

      <Footer />
    </div>
  )
}
