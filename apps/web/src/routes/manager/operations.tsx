import { createFileRoute } from '@tanstack/react-router'
import { DollarSign, ShieldCheck, User } from 'lucide-react'
import {
  useGetAllPayments,
  useGetAllUsers,
} from '../../api/generated/endpoints'
import type { PaymentDto, UserDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/manager/operations')({
  component: ManagerOperationsContent,
})

function ManagerOperationsContent() {
  const { data: paymentsData = [] } = useGetAllPayments()
  const payments = (paymentsData || []) as PaymentDto[]

  const { data: usersData = [] } = useGetAllUsers()
  const users = (usersData || []) as UserDto[]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <ShieldCheck className="w-4 h-4" />
            Executive Operations Suite
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Operations: Billing Ledgers, Users & System Audit
          </h1>
          <p className="text-xs text-muted-foreground">
            Audit financial transaction ledgers, manage system user accounts,
            and review security audit trails.
          </p>
        </section>

        {/* Financial Ledgers Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <DollarSign className="w-4 h-4 text-primary" />
            Master Financial Ledger & Billing
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

        {/* System User Accounts Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <User className="w-4 h-4 text-primary" />
            System User Accounts & Roles
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
