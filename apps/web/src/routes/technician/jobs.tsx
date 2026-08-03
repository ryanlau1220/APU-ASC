import { createFileRoute } from '@tanstack/react-router'
import { CheckCircle2, Clock, Wrench } from 'lucide-react'
import * as React from 'react'
import {
  useGetMyWorkOrders,
  useUpdateWorkOrderDiagnosticNotes,
  useUpdateWorkOrderStatus,
} from '../../api/generated/endpoints'
import type { UpdateWorkOrderStatusStatus } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import {
  workOrderTransitionLabels,
  workOrderTransitionTargets,
} from '../../lib/lifecycle'

export const Route = createFileRoute('/technician/jobs')({
  component: TechnicianJobsContent,
})

function TechnicianJobsContent() {
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: workOrders = [], refetch } = useGetMyWorkOrders()
  const [diagnosticNotes, setDiagnosticNotes] = React.useState<
    Record<string, string>
  >({})

  const updateStatusMutation = useUpdateWorkOrderStatus<Error>()
  const updateDiagnosticNotesMutation =
    useUpdateWorkOrderDiagnosticNotes<Error>()

  const handleUpdate = (id: string, status: string) => {
    setMessage(null)
    updateStatusMutation.mutate(
      { id, params: { status: status as UpdateWorkOrderStatusStatus } },
      {
        onSuccess: () => {
          setMessage('Work order status updated successfully!')
          refetch()
        },
        onError: (err: Error) => {
          setMessage(err.message || 'Failed to update work order status.')
        },
      },
    )
  }

  const handleSaveDiagnostics = (id: string) => {
    updateDiagnosticNotesMutation.mutate(
      { id, data: diagnosticNotes[id] || '' },
      {
        onSuccess: () => {
          setMessage('Diagnostic notes saved successfully!')
          refetch()
        },
        onError: (err: Error) => {
          setMessage(err.message || 'Failed to save diagnostic notes.')
        },
      },
    )
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <Wrench className="w-4 h-4" />
            Workshop Bay Work Orders
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Bay Job Management & Progress Tracker
          </h1>
          <p className="text-xs text-muted-foreground">
            Update work order progress through its controlled lifecycle: OPEN
            $\rightarrow$ DIAGNOSING $\rightarrow$ IN_PROGRESS $\rightarrow$
            COMPLETED.
          </p>
        </section>

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

        {/* Work Orders Grid */}
        <section className="space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Clock className="w-4 h-4 text-primary" />
            Active Bay Jobs
          </h2>

          {workOrders.length === 0 ? (
            <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
              No jobs currently in workshop queue.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {workOrders.map((j) => (
                <div
                  key={j.id}
                  className="bg-card border border-border rounded-xl p-5 space-y-4 shadow-sm"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono font-bold text-primary px-2.5 py-1 rounded bg-primary/10 border border-primary/30">
                      {j.id}
                    </span>
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold border ${
                        j.status === 'COMPLETED'
                          ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                          : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                      }`}
                    >
                      <CheckCircle2 className="w-3 h-3" />
                      {j.status}
                    </span>
                  </div>

                  <div>
                    <div className="text-xs font-semibold text-muted-foreground">
                      Vehicle:{' '}
                      <span className="font-mono text-foreground">
                        {j.vehicleId}
                      </span>
                    </div>
                    <div className="text-xs font-semibold text-muted-foreground mt-0.5">
                      Service:{' '}
                      <span className="text-foreground">{j.serviceId}</span>
                    </div>
                    <div className="text-xs text-muted-foreground mt-0.5">
                      Customer ID: {j.customerId}
                    </div>
                  </div>

                  <div className="pt-3 border-t border-border space-y-2">
                    <label
                      htmlFor={`status-${j.id}`}
                      className="block text-[11px] font-semibold text-muted-foreground"
                    >
                      Bay Action Status
                    </label>
                    <div className="flex flex-wrap gap-2">
                      {workOrderTransitionTargets(j.status).map((status) => (
                        <button
                          key={status}
                          type="button"
                          onClick={() => j.id && handleUpdate(j.id, status)}
                          disabled={updateStatusMutation.isPending}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors disabled:opacity-50 ${
                            status === 'COMPLETED'
                              ? 'border-status-completed/40 text-status-completed hover:bg-status-completed/10'
                              : status === 'CANCELLED'
                                ? 'border-destructive/40 text-destructive hover:bg-destructive/10'
                                : 'bg-muted hover:border-primary/40'
                          }`}
                        >
                          {workOrderTransitionLabels[status]}
                        </button>
                      ))}
                      {workOrderTransitionTargets(j.status).length === 0 && (
                        <span className="text-xs text-muted-foreground">
                          Final state
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label
                      htmlFor={`diagnostic-${j.id}`}
                      className="block text-[11px] font-semibold text-muted-foreground"
                    >
                      Diagnostic notes
                    </label>
                    <textarea
                      id={`diagnostic-${j.id}`}
                      rows={3}
                      value={
                        diagnosticNotes[j.id || ''] ?? j.diagnosticNotes ?? ''
                      }
                      onChange={(event) =>
                        j.id &&
                        setDiagnosticNotes((current) => ({
                          ...current,
                          [j.id as string]: event.target.value,
                        }))
                      }
                      className="w-full rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
                    />
                    <button
                      type="button"
                      onClick={() => j.id && handleSaveDiagnostics(j.id)}
                      disabled={updateDiagnosticNotesMutation.isPending}
                      className="rounded-lg border border-border px-3 py-1.5 text-xs font-semibold hover:bg-muted disabled:opacity-50"
                    >
                      Save diagnostics
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  )
}
