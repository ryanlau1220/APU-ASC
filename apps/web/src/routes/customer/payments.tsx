import { createFileRoute } from '@tanstack/react-router'
import { CheckCircle2, CreditCard, DollarSign, ShieldCheck } from 'lucide-react'
import * as React from 'react'
import {
  useCreateCheckoutSession,
  useGetMyPayments,
} from '../../api/generated/endpoints'
import type { PaymentDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/customer/payments')({
  component: CustomerPaymentsContent,
})

function CustomerPaymentsContent() {
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: paymentsData = [], refetch } = useGetMyPayments()
  const myPayments = (paymentsData || []) as PaymentDto[]

  React.useEffect(() => {
    const checkout = new URLSearchParams(window.location.search).get('checkout')
    if (checkout === 'success') {
      setMessage(
        'Payment submitted. Your invoice updates after Stripe confirms it.',
      )
      refetch()
    } else if (checkout === 'cancelled') {
      setMessage('Checkout was cancelled. Your invoice remains unpaid.')
    }
  }, [refetch])

  const checkoutMutation = useCreateCheckoutSession({
    mutation: {
      onSuccess: (session) => {
        if (!session.checkoutUrl) {
          setMessage('Unable to start secure checkout. Please try again.')
          return
        }
        window.location.assign(session.checkoutUrl)
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Unable to start secure checkout.')
      },
    },
  })

  const handlePay = (paymentId: string) => {
    setMessage(null)
    checkoutMutation.mutate({ id: paymentId })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <CreditCard className="w-4 h-4" />
            Billing & Invoices
          </div>
          <h1 className="font-heading text-2xl font-bold">
            My Invoices & Online Payments
          </h1>
          <p className="text-xs text-muted-foreground">
            Review service billing statements, inspect invoice breakdowns, and
            process secure online payments.
          </p>
        </section>

        {message && (
          <div
            className={`p-3 rounded-lg text-xs font-semibold ${
              message.includes('submitted')
                ? 'bg-status-completed/10 text-status-completed border border-status-completed/30'
                : 'bg-destructive/10 text-destructive border border-destructive/30'
            }`}
          >
            {message}
          </div>
        )}

        {/* Payments Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <DollarSign className="w-4 h-4 text-primary" />
            Issued Invoices
          </h2>

          {myPayments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No invoices generated for your account.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Invoice ID</th>
                    <th className="py-3 px-3">Appointment ID</th>
                    <th className="py-3 px-3">Amount</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {myPayments.map((p) => (
                    <tr
                      key={p.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {p.id}
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
                          <CheckCircle2 className="w-3 h-3" />
                          {p.paymentStatus || 'UNPAID'}
                        </span>
                      </td>
                      <td className="py-3.5 px-3">
                        {p.paymentStatus !== 'PAID' ? (
                          <button
                            type="button"
                            onClick={() => p.id && handlePay(p.id)}
                            disabled={checkoutMutation.isPending}
                            className="px-3 py-1 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity text-xs disabled:opacity-50"
                          >
                            {checkoutMutation.isPending
                              ? 'Opening...'
                              : 'Pay securely'}
                          </button>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-status-completed">
                            <ShieldCheck className="w-3.5 h-3.5" />
                            Paid
                          </span>
                        )}
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
