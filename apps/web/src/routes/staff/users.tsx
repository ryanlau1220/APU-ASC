import { createFileRoute } from '@tanstack/react-router'
import { Plus, User, UserCheck } from 'lucide-react'
import * as React from 'react'
import { useCreateUser, useGetAllUsers } from '../../api/generated/endpoints'
import type { UserDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/staff/users')({
  component: StaffUsersContent,
})

function StaffUsersContent() {
  const [username, setUsername] = React.useState('')
  const [email, setEmail] = React.useState('')
  const [fullName, setFullName] = React.useState('')
  const [phoneNumber, setPhoneNumber] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: usersData = [], refetch } = useGetAllUsers()
  const users = (usersData || []) as UserDto[]

  const createUserMutation = useCreateUser({
    mutation: {
      onSuccess: () => {
        setMessage('Customer account registered successfully!')
        setUsername('')
        setEmail('')
        setFullName('')
        setPhoneNumber('')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to register customer account.')
      },
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setMessage(null)
    createUserMutation.mutate({
      data: {
        username,
        email,
        fullName,
        role: 'CUSTOMER',
      },
    })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <UserCheck className="w-4 h-4" />
            Customer Directory
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Customer Directory & Account Intake
          </h1>
          <p className="text-xs text-muted-foreground">
            Search existing customer profiles or register new walk-in customer
            accounts.
          </p>
        </section>

        {/* Customer Registration Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Plus className="w-4 h-4 text-primary" />
            Register Walk-in Customer Profile
          </h2>

          {message && (
            <div
              className={`p-3 rounded-lg text-xs font-semibold ${
                message.includes('successfully')
                  ? 'bg-status-completed/10 text-status-completed border border-status-completed/30'
                  : 'bg-destructive/10 text-destructive border border-destructive/30'
              }`}
            >
              {message}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4"
          >
            <div>
              <label
                htmlFor="uName"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Username
              </label>
              <input
                id="uName"
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. john_doe"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="fName"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Full Name
              </label>
              <input
                id="fName"
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. John Doe"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="emailAddr"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Email Address
              </label>
              <input
                id="emailAddr"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. john@example.com"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="phoneNum"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Phone Number
              </label>
              <input
                id="phoneNum"
                type="text"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="e.g. +60123456789"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="sm:col-span-2 lg:col-span-4 flex justify-end">
              <button
                type="submit"
                disabled={createUserMutation.isPending}
                className="py-2.5 px-6 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {createUserMutation.isPending
                  ? 'Registering...'
                  : 'Register Customer Account'}
              </button>
            </div>
          </form>
        </section>

        {/* Customer Accounts Directory Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <User className="w-4 h-4 text-primary" />
            Customer Directory List
          </h2>

          {users.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No registered user accounts found.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">User ID</th>
                    <th className="py-3 px-3">Username</th>
                    <th className="py-3 px-3">Full Name</th>
                    <th className="py-3 px-3">Email</th>
                    <th className="py-3 px-3">Roles</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {users.map((u) => (
                    <tr
                      key={u.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {u.id}
                      </td>
                      <td className="py-3.5 px-3 font-medium">{u.username}</td>
                      <td className="py-3.5 px-3">{u.fullName || '-'}</td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {u.email}
                      </td>
                      <td className="py-3.5 px-3">
                        <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-primary/10 text-primary border border-primary/30">
                          {u.role || 'CUSTOMER'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  )
}
