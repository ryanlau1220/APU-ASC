import { createFileRoute } from '@tanstack/react-router'
import {
  CheckCircle2,
  CreditCard,
  DollarSign,
  Plus,
  ShieldCheck,
} from 'lucide-react'
import * as React from 'react'
import {
  useCreateInvoice,
  useGetAllPayments,
  useProcessPayment,
} from '../../api/generated/endpoints'
import type { PaymentDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

type WorkOrderPaymentDto = PaymentDto & { workOrderId?: string }

export const Route = createFileRoute('/staff/payments')({
  component: StaffPaymentsContent,
})

function StaffPaymentsContent() {
  const [workOrderId, setWorkOrderId] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: paymentsData = [], refetch } = useGetAllPayments()
  const payments = (paymentsData || []) as WorkOrderPaymentDto[]

  const createInvoiceMutation = useCreateInvoice({
    mutation: {
      onSuccess: () => {
        setMessage('Invoice created successfully!')
        setWorkOrderId('')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to create invoice.')
      },
    },
  })

  const processPaymentMutation = useProcessPayment({
    mutation: {
      onSuccess: () => {
        setMessage('Counter cash/card payment processed successfully!')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Payment processing failed.')
      },
    },
  })

  const handleCreateInvoice = (e: React.FormEvent) => {
    e.preventDefault()
    setMessage(null)
    createInvoiceMutation.mutate({ data: { workOrderId } })
  }

  const handleProcess = (paymentId: string) => {
    setMessage(null)
    processPaymentMutation.mutate({
      id: paymentId,
      params: {
        method: 'CASH',
      },
    })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <CreditCard className="w-4 h-4" />
            Counter POS & Invoicing
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Generate Invoices & Process Counter Payments
          </h1>
          <p className="text-xs text-muted-foreground">
            Create customer invoices for completed workshop services and process
            counter cash/card transactions.
          </p>
        </section>

        {/* Invoice Creation Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Plus className="w-4 h-4 text-primary" />
            Generate New Invoice
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
            onSubmit={handleCreateInvoice}
            className="grid grid-cols-1 sm:grid-cols-2 gap-4"
          >
            <div>
              <label
                htmlFor="aptIdInput"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Work Order ID
              </label>
              <input
                id="workOrderIdInput"
                type="text"
                required
                value={workOrderId}
                onChange={(e) => setWorkOrderId(e.target.value)}
                placeholder="e.g. WO-201"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="rounded-lg border border-primary/20 bg-primary/5 px-3 py-2 text-xs text-muted-foreground">
              The invoice total is taken from the customer-approved quotation.
            </div>

            <div className="sm:col-span-3 flex justify-end">
              <button
                type="submit"
                disabled={createInvoiceMutation.isPending}
                className="py-2.5 px-6 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {createInvoiceMutation.isPending
                  ? 'Generating...'
                  : 'Generate Invoice'}
              </button>
            </div>
          </form>
        </section>

        {/* Master Invoices Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <DollarSign className="w-4 h-4 text-primary" />
            Counter POS Billing Queue
          </h2>

          {payments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No invoices found in database.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Invoice ID</th>
                    <th className="py-3 px-3">Customer ID</th>
                    <th className="py-3 px-3">Work Order ID</th>
                    <th className="py-3 px-3">Amount</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3">Counter POS Action</th>
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
                        {p.workOrderId || p.appointmentId}
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
                            onClick={() => p.id && handleProcess(p.id)}
                            disabled={processPaymentMutation.isPending}
                            className="px-3 py-1 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity text-xs disabled:opacity-50"
                          >
                            Mark Paid (Cash/POS)
                          </button>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-status-completed">
                            <ShieldCheck className="w-3.5 h-3.5" />
                            Completed
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
