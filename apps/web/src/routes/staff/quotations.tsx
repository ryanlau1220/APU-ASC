import { createFileRoute } from '@tanstack/react-router'
import { FileText, Plus, Send, Trash2 } from 'lucide-react'
import * as React from 'react'
import {
  useCreateQuotationDraft,
  useGetAllQuotations,
  useGetAllWorkOrders,
  useSubmitQuotation,
} from '../../api/generated/endpoints'
import type {
  QuotationDto,
  QuotationLineDto,
  QuotationLineRequestDto,
} from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/staff/quotations')({
  component: StaffQuotationsContent,
})

type DraftLine = QuotationLineRequestDto & { key: string }
type Quote = Required<Omit<QuotationDto, 'items'>> & {
  items: Required<QuotationLineDto>[]
}

const initialLine = (): DraftLine => ({
  key: crypto.randomUUID(),
  description: '',
  quantity: 1,
  unitPrice: 0,
})

const money = new Intl.NumberFormat('en-MY', {
  style: 'currency',
  currency: 'MYR',
})

function StaffQuotationsContent() {
  const [message, setMessage] = React.useState<string | null>(null)
  const [workOrderId, setWorkOrderId] = React.useState('')
  const [notes, setNotes] = React.useState('')
  const [validUntil, setValidUntil] = React.useState('')
  const [lines, setLines] = React.useState<DraftLine[]>([initialLine()])

  const { data: workOrders = [] } = useGetAllWorkOrders()
  const { data: quotationData = [], refetch } = useGetAllQuotations()
  const quotations = quotationData as Quote[]
  const createMutation = useCreateQuotationDraft<Error>()
  const submitMutation = useSubmitQuotation<Error>()

  const estimateTotal = lines.reduce(
    (total, line) =>
      total + Number(line.quantity || 0) * Number(line.unitPrice || 0),
    0,
  )

  const updateLine = (
    key: string,
    field: keyof Omit<DraftLine, 'key'>,
    value: string | number,
  ) => {
    setLines((current) =>
      current.map((line) =>
        line.key === key ? { ...line, [field]: value } : line,
      ),
    )
  }

  const createDraft = (event: React.FormEvent) => {
    event.preventDefault()
    setMessage(null)
    if (
      !workOrderId ||
      !validUntil ||
      lines.some((line) => !line.description.trim())
    ) {
      setMessage(
        'Select a work order, validity date, and describe every line item.',
      )
      return
    }
    createMutation.mutate(
      {
        data: {
          workOrderId,
          notes: notes.trim() || undefined,
          validUntil,
          items: lines.map(({ description, quantity, unitPrice }) => ({
            description: description.trim(),
            quantity: Number(quantity),
            unitPrice: Number(unitPrice),
          })),
        },
      },
      {
        onSuccess: () => {
          setMessage(
            'Quotation draft created. Review it, then send it for approval.',
          )
          setWorkOrderId('')
          setNotes('')
          setValidUntil('')
          setLines([initialLine()])
          refetch()
        },
        onError: (error: Error) => setMessage(error.message),
      },
    )
  }

  const submit = (id: string) => {
    setMessage(null)
    submitMutation.mutate(
      { id },
      {
        onSuccess: () => {
          setMessage('Quotation sent to the customer for approval.')
          refetch()
        },
        onError: (error: Error) => setMessage(error.message),
      },
    )
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="relative overflow-hidden rounded-xl border border-primary/30 bg-card p-6 sm:p-8">
          <div className="absolute inset-y-0 right-0 w-1/3 bg-[radial-gradient(circle_at_top_right,rgba(245,158,11,0.18),transparent_65%)]" />
          <div className="relative max-w-2xl space-y-2">
            <div className="inline-flex items-center gap-2 text-xs font-semibold text-primary">
              <FileText className="h-4 w-4" />
              Customer Approval Desk
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Quoted work, clearly approved
            </h1>
            <p className="text-sm text-muted-foreground">
              Build a precise estimate from diagnostic findings. The customer
              sees this exact total; accepted quotes unlock workshop work and
              invoicing.
            </p>
          </div>
        </section>

        {message && (
          <div
            className={`rounded-lg border p-3 text-xs font-semibold ${
              message.includes('created') || message.includes('sent')
                ? 'border-status-completed/30 bg-status-completed/10 text-status-completed'
                : 'border-destructive/30 bg-destructive/10 text-destructive'
            }`}
          >
            {message}
          </div>
        )}

        <section className="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
          <form
            onSubmit={createDraft}
            className="rounded-xl border border-border bg-card p-6 space-y-5"
          >
            <div>
              <h2 className="font-heading text-lg font-bold">
                New quotation draft
              </h2>
              <p className="text-xs text-muted-foreground mt-1">
                Prices are recalculated and fixed by the server.
              </p>
            </div>
            <label className="block text-xs font-semibold text-muted-foreground">
              Work order
              <select
                value={workOrderId}
                onChange={(event) => setWorkOrderId(event.target.value)}
                required
                className="mt-1.5 w-full rounded-lg border border-border bg-input px-3 py-2 text-sm text-foreground outline-none focus:border-primary"
              >
                <option value="">Select an active work order</option>
                {workOrders
                  .filter(
                    (workOrder) =>
                      workOrder.status !== 'COMPLETED' &&
                      workOrder.status !== 'CANCELLED',
                  )
                  .map((workOrder) => (
                    <option key={workOrder.id} value={workOrder.id}>
                      {workOrder.id} · {workOrder.customerId} ·{' '}
                      {workOrder.status}
                    </option>
                  ))}
              </select>
            </label>
            <div className="grid gap-4 sm:grid-cols-[1fr_10rem]">
              <label className="block text-xs font-semibold text-muted-foreground">
                Notes for customer
                <textarea
                  rows={2}
                  value={notes}
                  onChange={(event) => setNotes(event.target.value)}
                  className="mt-1.5 w-full rounded-lg border border-border bg-input px-3 py-2 text-sm text-foreground outline-none focus:border-primary"
                />
              </label>
              <label className="block text-xs font-semibold text-muted-foreground">
                Valid until
                <input
                  type="date"
                  value={validUntil}
                  onChange={(event) => setValidUntil(event.target.value)}
                  required
                  className="mt-1.5 w-full rounded-lg border border-border bg-input px-3 py-2 text-sm text-foreground outline-none focus:border-primary"
                />
              </label>
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-muted-foreground">
                  Line items
                </span>
                <button
                  type="button"
                  onClick={() =>
                    setLines((current) => [...current, initialLine()])
                  }
                  className="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:underline"
                >
                  <Plus className="h-3.5 w-3.5" /> Add line
                </button>
              </div>
              {lines.map((line, index) => (
                <div
                  key={line.key}
                  className="grid grid-cols-[1fr_4.5rem_6rem_auto] gap-2"
                >
                  <input
                    aria-label={`Line ${index + 1} description`}
                    placeholder="Parts or labour description"
                    value={line.description}
                    onChange={(event) =>
                      updateLine(line.key, 'description', event.target.value)
                    }
                    className="min-w-0 rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
                  />
                  <input
                    aria-label={`Line ${index + 1} quantity`}
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={line.quantity}
                    onChange={(event) =>
                      updateLine(line.key, 'quantity', event.target.value)
                    }
                    className="rounded-lg border border-border bg-input px-2 py-2 text-xs outline-none focus:border-primary"
                  />
                  <input
                    aria-label={`Line ${index + 1} price`}
                    type="number"
                    min="0"
                    step="0.01"
                    value={line.unitPrice}
                    onChange={(event) =>
                      updateLine(line.key, 'unitPrice', event.target.value)
                    }
                    className="rounded-lg border border-border bg-input px-2 py-2 text-xs outline-none focus:border-primary"
                  />
                  <button
                    type="button"
                    aria-label={`Remove line ${index + 1}`}
                    disabled={lines.length === 1}
                    onClick={() =>
                      setLines((current) =>
                        current.filter((item) => item.key !== line.key),
                      )
                    }
                    className="rounded-lg border border-border px-2 text-muted-foreground hover:text-destructive disabled:opacity-30"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>
            <div className="flex items-center justify-between rounded-lg border border-primary/20 bg-primary/5 px-4 py-3">
              <span className="text-xs font-semibold text-muted-foreground">
                Estimated total
              </span>
              <span className="font-heading text-lg font-bold text-primary">
                {money.format(estimateTotal)}
              </span>
            </div>
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="w-full rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground hover:opacity-90 disabled:opacity-50"
            >
              Save quotation draft
            </button>
          </form>

          <section className="rounded-xl border border-border bg-card p-6 space-y-4">
            <div>
              <h2 className="font-heading text-lg font-bold">Quote queue</h2>
              <p className="text-xs text-muted-foreground mt-1">
                Drafts can be submitted once. Submitted amounts are immutable.
              </p>
            </div>
            <div className="space-y-3">
              {quotations.length === 0 ? (
                <p className="rounded-lg border border-dashed border-border p-6 text-center text-xs text-muted-foreground">
                  No quotations created yet.
                </p>
              ) : (
                quotations.map((quote) => (
                  <article
                    key={quote.id}
                    className="rounded-lg border border-border p-4 space-y-3"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="font-mono text-xs font-bold text-primary">
                          {quote.quoteNumber} · R{quote.revision}
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {quote.workOrderId} · valid through {quote.validUntil}
                        </div>
                      </div>
                      <span className="rounded-full border border-border bg-muted px-2 py-1 text-[10px] font-bold">
                        {quote.status.replace('_', ' ')}
                      </span>
                    </div>
                    <div className="flex items-end justify-between gap-3">
                      <span className="font-heading text-xl font-bold">
                        {money.format(quote.totalAmount)}
                      </span>
                      {quote.status === 'DRAFT' && (
                        <button
                          type="button"
                          onClick={() => submit(quote.id)}
                          disabled={submitMutation.isPending}
                          className="inline-flex items-center gap-1 rounded-lg border border-primary/40 px-3 py-1.5 text-xs font-semibold text-primary hover:bg-primary/10 disabled:opacity-50"
                        >
                          <Send className="h-3.5 w-3.5" /> Send for approval
                        </button>
                      )}
                    </div>
                  </article>
                ))
              )}
            </div>
          </section>
        </section>
      </main>
      <Footer />
    </div>
  )
}
