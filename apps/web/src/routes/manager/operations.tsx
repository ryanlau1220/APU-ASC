import { createFileRoute } from '@tanstack/react-router'
import { DollarSign, Mail, ShieldCheck, UserPlus, Users } from 'lucide-react'
import * as React from 'react'
import {
  useCreateUser,
  useGetAllPayments,
  useGetAllUsers,
  useUpdateUserStatus,
} from '../../api/generated/endpoints'
import type { PaymentDto, UserDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/manager/operations')({
  component: ManagerOperationsContent,
})

function ManagerOperationsContent() {
  const [isModalOpen, setIsModalOpen] = React.useState(false)
  const [username, setUsername] = React.useState('')
  const [fullName, setFullName] = React.useState('')
  const [email, setEmail] = React.useState('')
  const [role, setRole] = React.useState<
    'STAFF' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER'
  >('STAFF')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: paymentsData = [] } = useGetAllPayments()
  const payments = (paymentsData || []) as PaymentDto[]

  const { data: usersData = [], refetch: refetchUsers } = useGetAllUsers()
  const users = (usersData || []) as UserDto[]

  const createUserMutation = useCreateUser({
    mutation: {
      onSuccess: (newUser) => {
        setMessage(
          `User account '${newUser.username}' created successfully! An email invite has been dispatched to ${newUser.email}.`,
        )
        setIsModalOpen(false)
        setUsername('')
        setFullName('')
        setEmail('')
        setRole('STAFF')
        refetchUsers()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to create user account.')
      },
    },
  })

  const updateStatusMutation = useUpdateUserStatus({
    mutation: {
      onSuccess: () => {
        setMessage('User status updated successfully!')
        refetchUsers()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to update user status.')
      },
    },
  })

  const handleCreateUser = (e: React.FormEvent) => {
    e.preventDefault()
    if (!username || !email) {
      setMessage('Username and Email are required.')
      return
    }

    createUserMutation.mutate({
      data: {
        username,
        fullName,
        email,
        role: role as 'STAFF' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER',
        status: 'ACTIVE',
      },
    })
  }

  const handleToggleStatus = (user: UserDto) => {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    if (user.id) {
      updateStatusMutation.mutate({
        id: user.id,
        params: { status: nextStatus },
      })
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Banner Section */}
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1 max-w-2xl">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <ShieldCheck className="w-4 h-4" />
              Executive Operations Suite
            </div>
            <h1 className="font-heading text-2xl font-bold tracking-tight">
              Operations: User Management & Financial Audits
            </h1>
            <p className="text-xs text-muted-foreground">
              Provision internal accounts (Staff, Technicians, Managers) with
              email password invite, manage user statuses, and inspect billing
              ledgers.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity"
          >
            <UserPlus className="w-4 h-4" />
            Provision New User
          </button>
        </section>

        {message && (
          <div className="p-4 rounded-xl border border-primary/30 bg-primary/10 text-xs font-semibold text-primary flex items-center justify-between">
            <span>{message}</span>
            <button
              type="button"
              onClick={() => setMessage(null)}
              className="text-primary hover:underline ml-4"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Modal for Provisioning User */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-card border border-border rounded-xl max-w-md w-full p-6 space-y-6 shadow-2xl">
              <div className="flex items-center justify-between border-b border-border pb-4">
                <div className="flex items-center gap-2 font-heading font-bold text-lg">
                  <UserPlus className="w-5 h-5 text-primary" />
                  Provision Employee Account
                </div>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="text-muted-foreground hover:text-foreground text-sm font-bold"
                >
                  ✕
                </button>
              </div>

              <form onSubmit={handleCreateUser} className="space-y-4 text-xs">
                <div className="space-y-1">
                  <label
                    htmlFor="user-role"
                    className="font-semibold text-muted-foreground"
                  >
                    Account Role
                  </label>
                  <select
                    id="user-role"
                    value={role}
                    onChange={(e) =>
                      setRole(
                        e.target.value as
                          | 'STAFF'
                          | 'TECHNICIAN'
                          | 'MANAGER'
                          | 'CUSTOMER',
                      )
                    }
                    className="w-full px-3 py-2 rounded-lg bg-background border border-border focus:border-primary outline-none"
                  >
                    <option value="STAFF">Staff</option>
                    <option value="TECHNICIAN">Technician</option>
                    <option value="MANAGER">Manager</option>
                    <option value="CUSTOMER">Customer</option>
                  </select>
                </div>

                <div className="space-y-1">
                  <label
                    htmlFor="user-username"
                    className="font-semibold text-muted-foreground"
                  >
                    Username *
                  </label>
                  <input
                    id="user-username"
                    type="text"
                    required
                    placeholder="e.g. staff_john"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg bg-background border border-border focus:border-primary outline-none"
                  />
                </div>

                <div className="space-y-1">
                  <label
                    htmlFor="user-fullname"
                    className="font-semibold text-muted-foreground"
                  >
                    Full Name
                  </label>
                  <input
                    id="user-fullname"
                    type="text"
                    placeholder="e.g. John Doe"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg bg-background border border-border focus:border-primary outline-none"
                  />
                </div>

                <div className="space-y-1">
                  <label
                    htmlFor="user-email"
                    className="font-semibold text-muted-foreground"
                  >
                    Email Address (Password Setup Invite Sent Here) *
                  </label>
                  <input
                    id="user-email"
                    type="email"
                    required
                    placeholder="e.g. john.doe@apu-asc.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg bg-background border border-border focus:border-primary outline-none"
                  />
                </div>

                <div className="p-3 rounded-lg bg-muted text-[11px] text-muted-foreground space-y-1">
                  <div className="font-semibold text-foreground flex items-center gap-1.5">
                    <Mail className="w-3.5 h-3.5 text-primary" />
                    Security Best Practice Notice:
                  </div>
                  <div>
                    No initial password required. An automated welcome email
                    invite will be sent to the employee to set their private
                    password.
                  </div>
                </div>

                <div className="flex items-center justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setIsModalOpen(false)}
                    className="px-4 py-2 rounded-lg border border-border hover:bg-muted text-xs font-semibold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={createUserMutation.isPending}
                    className="px-4 py-2 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
                  >
                    {createUserMutation.isPending
                      ? 'Provisioning...'
                      : 'Send Email Invite & Provision'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* System User Accounts Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-heading text-lg font-bold flex items-center gap-2">
              <Users className="w-4 h-4 text-primary" />
              System Account Directory & Status Control
            </h2>
            <span className="text-xs text-muted-foreground font-medium">
              Total Accounts: {users.length}
            </span>
          </div>

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
                    <th className="py-3 px-3">Role</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3 text-right">Actions</th>
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
                      <td className="py-3.5 px-3">
                        <span
                          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-semibold border ${
                            u.status === 'ACTIVE' || !u.status
                              ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                              : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                          }`}
                        >
                          {u.status || 'ACTIVE'}
                        </span>
                      </td>
                      <td className="py-3.5 px-3 text-right">
                        <button
                          type="button"
                          onClick={() => handleToggleStatus(u)}
                          disabled={updateStatusMutation.isPending}
                          className="px-2.5 py-1 rounded border border-border hover:bg-muted text-[11px] font-semibold transition-colors"
                        >
                          {u.status === 'ACTIVE' || !u.status
                            ? 'Deactivate'
                            : 'Activate'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Financial Ledgers Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <DollarSign className="w-4 h-4 text-primary" />
            Master Financial Ledger & Billing Audits
          </h2>

          {payments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No financial records found in database.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Invoice ID</th>
                    <th className="py-3 px-3">Customer ID</th>
                    <th className="py-3 px-3">Appointment ID</th>
                    <th className="py-3 px-3">Amount</th>
                    <th className="py-3 px-3">Payment Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {payments.map((p) => (
                    <tr
                      key={p.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {p.id}
                      </td>
                      <td className="py-3.5 px-3 font-medium">
                        {p.customerId}
                      </td>
                      <td className="py-3.5 px-3 font-mono">
                        {p.appointmentId}
                      </td>
                      <td className="py-3.5 px-3 font-bold text-primary">
                        RM {Number(p.amount || 0).toFixed(2)}
                      </td>
                      <td className="py-3.5 px-3">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border ${
                            p.paymentStatus === 'PAID'
                              ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                              : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                          }`}
                        >
                          {p.paymentStatus || 'UNPAID'}
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
