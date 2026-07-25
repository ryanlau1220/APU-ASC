import { createFileRoute } from '@tanstack/react-router'
import { CheckCircle2, CreditCard, FileText } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/payments')({ component: PaymentsPage })

function PaymentsPage() {
  const payments = [
    {
      id: 'PAY-801',
      invoiceNumber: 'INV-2026-001',
      appointmentId: 'APT-1003',
      customer: 'Devon Lee (USR-103)',
      amount: 150.0,
      paymentMethod: 'CREDIT_CARD',
      status: 'PAID',
      paidAt: '2026-07-25 17:15:00',
    },
    {
      id: 'PAY-802',
      invoiceNumber: 'INV-2026-002',
      appointmentId: 'APT-1001',
      customer: 'Alex Tan (USR-101)',
      amount: 180.0,
      paymentMethod: 'ONLINE_BANKING',
      status: 'UNPAID',
      paidAt: '-',
    },
  ]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <CreditCard className="w-4 h-4" />
              Billing & Invoices
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Payments & Receipts
            </h1>
            <p className="text-xs text-muted-foreground">
              Review invoice statements, payment processing status, and
              transaction methods.
            </p>
          </div>
        </section>

        {/* Payments Grid */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {payments.map((p) => (
            <div
              key={p.id}
              className="bg-card border border-border rounded-xl p-5 space-y-4"
            >
              <div className="flex items-center justify-between border-b border-border pb-3">
                <div className="flex items-center gap-2">
                  <FileText className="w-4 h-4 text-primary" />
                  <span className="font-mono text-xs font-bold text-foreground">
                    {p.invoiceNumber}
                  </span>
                </div>

                <span
                  className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border ${
                    p.status === 'PAID'
                      ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                      : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                  }`}
                >
                  <CheckCircle2 className="w-3 h-3" />
                  {p.status}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-4 text-xs">
                <div>
                  <span className="text-muted-foreground block mb-0.5">
                    Customer
                  </span>
                  <div className="font-semibold">{p.customer}</div>
                </div>

                <div>
                  <span className="text-muted-foreground block mb-0.5">
                    Payment Method
                  </span>
                  <div className="font-semibold">{p.paymentMethod}</div>
                </div>
              </div>

              <div className="pt-3 border-t border-border flex items-center justify-between">
                <div className="text-[11px] text-muted-foreground">
                  Paid: {p.paidAt}
                </div>
                <div className="font-heading text-xl font-bold text-foreground">
                  RM {p.amount.toFixed(2)}
                </div>
              </div>
            </div>
          ))}
        </section>
      </main>

      <Footer />
    </div>
  )
}
