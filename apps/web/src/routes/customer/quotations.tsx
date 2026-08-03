import { createFileRoute } from '@tanstack/react-router'
import { Check, ClipboardCheck, X } from 'lucide-react'
import * as React from 'react'
import { useDecideQuotation, useGetMyQuotations } from '../../api/quotations'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/customer/quotations')({
  component: CustomerQuotationsContent,
})

const money = new Intl.NumberFormat('en-MY', {
  style: 'currency',
  currency: 'MYR',
})

function CustomerQuotationsContent() {
  const [message, setMessage] = React.useState<string | null>(null)
  const [responseNotes, setResponseNotes] = React.useState<
    Record<string, string>
  >({})
  const { data: quotations = [], refetch } = useGetMyQuotations()
  const decisionMutation = useDecideQuotation()

  const decide = (id: string, decision: 'APPROVE' | 'REJECT') => {
    setMessage(null)
    decisionMutation.mutate(
      { id, decision, responseNotes: responseNotes[id]?.trim() || undefined },
      {
        onSuccess: () => {
          setMessage(
            decision === 'APPROVE'
              ? 'Quotation approved. The workshop can now begin the agreed work.'
              : 'Quotation declined. Staff can prepare a revised estimate.',
          )
          refetch()
        },
        onError: (error: Error) => setMessage(error.message),
      },
    )
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />
      <main className="flex-1 max-w-5xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="rounded-xl border border-primary/30 bg-card p-6 sm:p-8">
          <div className="inline-flex items-center gap-2 text-xs font-semibold text-primary">
            <ClipboardCheck className="h-4 w-4" />
            Service authorisations
          </div>
          <h1 className="mt-2 font-heading text-2xl font-bold">
            Review before work begins
          </h1>
          <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
            Each estimate lists the recommended parts and labour. Approval
            authorises only the stated total; staff must send a revised
            quotation for additional work.
          </p>
        </section>

        {message && (
          <div
            className={`rounded-lg border p-3 text-xs font-semibold ${
              message.includes('approved') || message.includes('declined')
                ? 'border-status-completed/30 bg-status-completed/10 text-status-completed'
                : 'border-destructive/30 bg-destructive/10 text-destructive'
            }`}
          >
            {message}
          </div>
        )}

        {quotations.length === 0 ? (
          <section className="rounded-xl border border-dashed border-border bg-card p-10 text-center text-sm text-muted-foreground">
            There are no service quotations awaiting your review.
          </section>
        ) : (
          <section className="space-y-5">
            {quotations.map((quote) => {
              const pending = quote.status === 'PENDING_APPROVAL'
              return (
                <article
                  key={quote.id}
                  className="overflow-hidden rounded-xl border border-border bg-card"
                >
                  <div className="flex flex-col gap-3 border-b border-border bg-muted/30 p-5 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <div className="font-mono text-xs font-bold text-primary">
                        {quote.quoteNumber} · Revision {quote.revision}
                      </div>
                      <h2 className="mt-1 font-heading text-lg font-bold">
                        Work order {quote.workOrderId}
                      </h2>
                      <p className="mt-1 text-xs text-muted-foreground">
                        Valid until {quote.validUntil}
                      </p>
                    </div>
                    <span
                      className={`w-fit rounded-full border px-2.5 py-1 text-[10px] font-bold ${
                        quote.status === 'APPROVED'
                          ? 'border-status-completed/30 bg-status-completed/10 text-status-completed'
                          : quote.status === 'REJECTED' ||
                              quote.status === 'EXPIRED'
                            ? 'border-destructive/30 bg-destructive/10 text-destructive'
                            : 'border-status-pending/30 bg-status-pending/10 text-status-pending'
                      }`}
                    >
                      {quote.status.replace('_', ' ')}
                    </span>
                  </div>
                  <div className="p-5 space-y-5">
                    {quote.notes && (
                      <p className="text-sm text-muted-foreground">
                        {quote.notes}
                      </p>
                    )}
                    <div className="overflow-x-auto">
                      <table className="w-full text-left text-xs">
                        <thead>
                          <tr className="border-b border-border text-muted-foreground">
                            <th className="px-2 py-2">Recommended work</th>
                            <th className="px-2 py-2 text-right">Qty</th>
                            <th className="px-2 py-2 text-right">Unit price</th>
                            <th className="px-2 py-2 text-right">Amount</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border">
                          {quote.items.map((item) => (
                            <tr key={item.id || item.description}>
                              <td className="px-2 py-3 font-medium">
                                {item.description}
                              </td>
                              <td className="px-2 py-3 text-right">
                                {item.quantity}
                              </td>
                              <td className="px-2 py-3 text-right">
                                {money.format(item.unitPrice)}
                              </td>
                              <td className="px-2 py-3 text-right font-semibold">
                                {money.format(
                                  item.lineTotal ??
                                    item.quantity * item.unitPrice,
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                        <tfoot>
                          <tr>
                            <td
                              colSpan={3}
                              className="px-2 pt-4 text-right text-sm font-semibold"
                            >
                              Total authorised amount
                            </td>
                            <td className="px-2 pt-4 text-right font-heading text-lg font-bold text-primary">
                              {money.format(quote.totalAmount)}
                            </td>
                          </tr>
                        </tfoot>
                      </table>
                    </div>

                    {pending ? (
                      <div className="rounded-lg border border-primary/20 bg-primary/5 p-4 space-y-3">
                        <label className="block text-xs font-semibold text-muted-foreground">
                          Optional response note
                          <textarea
                            rows={2}
                            value={responseNotes[quote.id] ?? ''}
                            onChange={(event) =>
                              setResponseNotes((current) => ({
                                ...current,
                                [quote.id]: event.target.value,
                              }))
                            }
                            placeholder="For example: Please proceed, or explain what needs revising."
                            className="mt-1.5 w-full rounded-lg border border-border bg-input px-3 py-2 text-sm text-foreground outline-none focus:border-primary"
                          />
                        </label>
                        <div className="flex flex-wrap gap-2">
                          <button
                            type="button"
                            onClick={() => decide(quote.id, 'APPROVE')}
                            disabled={decisionMutation.isPending}
                            className="inline-flex items-center gap-1.5 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground hover:opacity-90 disabled:opacity-50"
                          >
                            <Check className="h-4 w-4" /> Approve{' '}
                            {money.format(quote.totalAmount)}
                          </button>
                          <button
                            type="button"
                            onClick={() => decide(quote.id, 'REJECT')}
                            disabled={decisionMutation.isPending}
                            className="inline-flex items-center gap-1.5 rounded-lg border border-destructive/40 px-4 py-2 text-sm font-semibold text-destructive hover:bg-destructive/10 disabled:opacity-50"
                          >
                            <X className="h-4 w-4" /> Request revision
                          </button>
                        </div>
                      </div>
                    ) : (
                      quote.responseNotes && (
                        <p className="rounded-lg bg-muted p-3 text-xs text-muted-foreground">
                          Response: {quote.responseNotes}
                        </p>
                      )
                    )}
                  </div>
                </article>
              )
            })}
          </section>
        )}
      </main>
      <Footer />
    </div>
  )
}
