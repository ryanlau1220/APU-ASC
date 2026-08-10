import {
  CheckCircle2,
  ClipboardList,
  Loader2,
  Search,
  UserRoundCog,
} from 'lucide-react'
import * as React from 'react'
import {
  useGetAllUsers,
  useGetAllWorkOrders,
  useUpdateWorkOrder,
  useUpdateWorkOrderDiagnosticNotes,
  useUpdateWorkOrderStatus,
} from '../api/generated/endpoints'
import type {
  UpdateWorkOrderStatusStatus,
  UserDto,
  WorkOrderDto,
} from '../api/generated/models'
import {
  workOrderTransitionLabels,
  workOrderTransitionTargets,
} from '../lib/lifecycle'
import { WorkOrderDocuments } from './WorkOrderDocuments'

const WORK_ORDER_STATUSES = [
  'OPEN',
  'DIAGNOSING',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
] as const

function statusClass(status?: string) {
  if (status === 'COMPLETED') {
    return 'border-status-completed/30 bg-status-completed/10 text-status-completed'
  }
  if (status === 'CANCELLED') {
    return 'border-destructive/30 bg-destructive/10 text-destructive'
  }
  return 'border-status-pending/30 bg-status-pending/10 text-status-pending'
}

function technicianLabel(technician?: UserDto) {
  if (!technician) return 'Unassigned'
  return (
    technician.fullName ||
    technician.username ||
    technician.id ||
    'Unknown technician'
  )
}

export function WorkOrderOperations() {
  const [query, setQuery] = React.useState('')
  const [statusFilter, setStatusFilter] = React.useState('ALL')
  const [selectedId, setSelectedId] = React.useState<string | undefined>()
  const [technicianId, setTechnicianId] = React.useState('')
  const [diagnosticNotes, setDiagnosticNotes] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: workOrderData = [], isLoading, refetch } = useGetAllWorkOrders()
  const { data: userData = [] } = useGetAllUsers()
  const workOrders = workOrderData as WorkOrderDto[]
  const users = userData as UserDto[]
  const technicians = users.filter((user) => user.role === 'TECHNICIAN')
  const activeTechnicians = technicians.filter(
    (technician) => technician.status === 'ACTIVE',
  )
  const selected = workOrders.find((workOrder) => workOrder.id === selectedId)
  const normalizedQuery = query.trim().toLowerCase()
  const visibleWorkOrders = workOrders.filter((workOrder) => {
    if (statusFilter !== 'ALL' && workOrder.status !== statusFilter)
      return false
    if (!normalizedQuery) return true
    return [
      workOrder.id,
      workOrder.vehicleId,
      workOrder.customerId,
      workOrder.serviceId,
      workOrder.technicianId,
    ].some((value) => value?.toLowerCase().includes(normalizedQuery))
  })

  const updateWorkOrder = useUpdateWorkOrder<Error>()
  const updateStatus = useUpdateWorkOrderStatus<Error>()
  const updateDiagnostics = useUpdateWorkOrderDiagnosticNotes<Error>()

  React.useEffect(() => {
    if (!selectedId && workOrders[0]?.id) {
      setSelectedId(workOrders[0].id)
    }
    if (
      selectedId &&
      !workOrders.some((workOrder) => workOrder.id === selectedId)
    ) {
      setSelectedId(workOrders[0]?.id)
    }
  }, [selectedId, workOrders])

  React.useEffect(() => {
    setTechnicianId(selected?.technicianId || '')
    setDiagnosticNotes(selected?.diagnosticNotes || '')
  }, [selected?.technicianId, selected?.diagnosticNotes])

  const refreshWorkOrders = () => {
    void refetch()
  }

  const handleAssignment = () => {
    if (!selected?.id || !technicianId) {
      setMessage('Select an active technician before saving the assignment.')
      return
    }
    setMessage(null)
    updateWorkOrder.mutate(
      { id: selected.id, data: { ...selected, technicianId } },
      {
        onSuccess: () => {
          setMessage('Technician assignment saved.')
          refreshWorkOrders()
        },
        onError: (error) =>
          setMessage(error.message || 'Unable to save assignment.'),
      },
    )
  }

  const handleStatus = (status: UpdateWorkOrderStatusStatus) => {
    if (!selected?.id) return
    setMessage(null)
    updateStatus.mutate(
      { id: selected.id, params: { status } },
      {
        onSuccess: () => {
          setMessage('Work-order status updated.')
          refreshWorkOrders()
        },
        onError: (error) =>
          setMessage(error.message || 'Unable to update status.'),
      },
    )
  }

  const handleDiagnostics = () => {
    if (!selected?.id) return
    setMessage(null)
    updateDiagnostics.mutate(
      { id: selected.id, data: diagnosticNotes },
      {
        onSuccess: () => {
          setMessage('Diagnostic notes saved.')
          refreshWorkOrders()
        },
        onError: (error) =>
          setMessage(error.message || 'Unable to save diagnostic notes.'),
      },
    )
  }

  return (
    <div className="space-y-6">
      <section className="space-y-2 rounded-xl border border-border bg-card p-6 sm:p-8">
        <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
          <ClipboardList className="h-4 w-4" /> Workshop operations
        </div>
        <h1 className="font-heading text-2xl font-bold tracking-tight">
          Work-order control centre
        </h1>
        <p className="max-w-3xl text-xs text-muted-foreground">
          Assign technicians, progress authorised work, record diagnostics, and
          review workshop evidence from one operational queue.
        </p>
      </section>

      {message && (
        <output
          className={`block rounded-lg border p-3 text-xs font-semibold ${
            message.includes('saved') || message.includes('updated')
              ? 'border-status-completed/30 bg-status-completed/10 text-status-completed'
              : 'border-destructive/30 bg-destructive/10 text-destructive'
          }`}
        >
          {message}
        </output>
      )}

      <section className="grid gap-6 xl:grid-cols-[minmax(19rem,0.85fr)_minmax(0,1.5fr)]">
        <div className="space-y-3 rounded-xl border border-border bg-card p-4 sm:p-5">
          <div className="flex flex-col gap-2 sm:flex-row xl:flex-col">
            <label className="relative block min-w-0 flex-1">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <span className="sr-only">Search work orders</span>
              <input
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Search work order, vehicle, customer…"
                className="w-full rounded-lg border border-border bg-input py-2 pl-9 pr-3 text-xs outline-none focus:border-primary"
              />
            </label>
            <label className="sr-only" htmlFor="work-order-status-filter">
              Filter by status
            </label>
            <select
              id="work-order-status-filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className="rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
            >
              <option value="ALL">All statuses</option>
              {WORK_ORDER_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center justify-between gap-3">
            <h2 className="font-heading text-base font-bold">
              Work-order queue
            </h2>
            <span className="text-xs text-muted-foreground">
              {visibleWorkOrders.length} shown
            </span>
          </div>

          {isLoading ? (
            <div className="flex justify-center py-10">
              <Loader2 className="h-5 w-5 animate-spin text-primary" />
            </div>
          ) : visibleWorkOrders.length === 0 ? (
            <p className="rounded-lg border border-dashed border-border p-6 text-center text-xs text-muted-foreground">
              No work orders match the current filter.
            </p>
          ) : (
            <ul className="max-h-[34rem] space-y-2 overflow-y-auto pr-1">
              {visibleWorkOrders.map((workOrder) => {
                const technician = technicians.find(
                  (user) => user.id === workOrder.technicianId,
                )
                const isSelected = workOrder.id === selected?.id
                return (
                  <li key={workOrder.id}>
                    <button
                      type="button"
                      onClick={() =>
                        workOrder.id && setSelectedId(workOrder.id)
                      }
                      className={`w-full rounded-lg border p-3 text-left transition-colors hover:border-primary/50 ${
                        isSelected
                          ? 'border-primary bg-primary/10'
                          : 'border-border bg-background'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <span className="font-mono text-xs font-bold text-primary">
                          {workOrder.id}
                        </span>
                        <span
                          className={`rounded-full border px-2 py-0.5 text-[10px] font-semibold ${statusClass(workOrder.status)}`}
                        >
                          {workOrder.status}
                        </span>
                      </div>
                      <p className="mt-2 text-xs font-medium text-foreground">
                        {workOrder.vehicleId || 'Vehicle not recorded'} ·{' '}
                        {workOrder.serviceId || 'Service not recorded'}
                      </p>
                      <p className="mt-1 text-[11px] text-muted-foreground">
                        {technicianLabel(technician)}
                      </p>
                    </button>
                  </li>
                )
              })}
            </ul>
          )}
        </div>

        <section className="rounded-xl border border-border bg-card p-5 sm:p-6">
          {!selected ? (
            <div className="flex min-h-64 items-center justify-center text-center text-xs text-muted-foreground">
              Select a work order to manage its assignment, progress, and
              evidence.
            </div>
          ) : (
            <div className="space-y-6">
              <div className="flex flex-col justify-between gap-3 border-b border-border pb-4 sm:flex-row sm:items-start">
                <div>
                  <p className="text-xs font-semibold text-primary">
                    Selected work order
                  </p>
                  <h2 className="mt-1 font-mono text-base font-bold text-foreground">
                    {selected.id}
                  </h2>
                  <p className="mt-1 text-xs text-muted-foreground">
                    Vehicle {selected.vehicleId || '—'} · Customer{' '}
                    {selected.customerId || '—'} · Service{' '}
                    {selected.serviceId || '—'}
                  </p>
                </div>
                <span
                  className={`inline-flex w-fit items-center gap-1 rounded-full border px-2.5 py-1 text-xs font-semibold ${statusClass(selected.status)}`}
                >
                  <CheckCircle2 className="h-3.5 w-3.5" /> {selected.status}
                </span>
              </div>

              <section className="space-y-3">
                <div className="flex items-center gap-2">
                  <UserRoundCog className="h-4 w-4 text-primary" />
                  <h3 className="text-sm font-semibold">
                    Technician assignment
                  </h3>
                </div>
                <div className="flex flex-col gap-2 sm:flex-row">
                  <label className="sr-only" htmlFor="work-order-technician">
                    Assigned technician
                  </label>
                  <select
                    id="work-order-technician"
                    value={technicianId}
                    onChange={(event) => setTechnicianId(event.target.value)}
                    className="min-w-0 flex-1 rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
                  >
                    <option value="">Select active technician</option>
                    {activeTechnicians.map((technician) => (
                      <option key={technician.id} value={technician.id}>
                        {technicianLabel(technician)}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    onClick={handleAssignment}
                    disabled={updateWorkOrder.isPending || !technicianId}
                    className="rounded-lg border border-border px-3 py-2 text-xs font-semibold hover:bg-muted disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {selected.technicianId ? 'Reassign' : 'Assign'}
                  </button>
                </div>
                {activeTechnicians.length === 0 && (
                  <p className="text-xs text-destructive">
                    No active technician accounts are available for assignment.
                  </p>
                )}
              </section>

              <section className="space-y-3 border-t border-border pt-5">
                <h3 className="text-sm font-semibold">Execution status</h3>
                <div className="flex flex-wrap gap-2">
                  {workOrderTransitionTargets(selected.status).map((status) => (
                    <button
                      key={status}
                      type="button"
                      onClick={() => handleStatus(status)}
                      disabled={updateStatus.isPending}
                      className={`rounded-lg border px-3 py-2 text-xs font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
                        status === 'COMPLETED'
                          ? 'border-status-completed/40 text-status-completed hover:bg-status-completed/10'
                          : status === 'CANCELLED'
                            ? 'border-destructive/40 text-destructive hover:bg-destructive/10'
                            : 'border-border hover:bg-muted'
                      }`}
                    >
                      {workOrderTransitionLabels[status]}
                    </button>
                  ))}
                  {workOrderTransitionTargets(selected.status).length === 0 && (
                    <span className="text-xs text-muted-foreground">
                      Final state
                    </span>
                  )}
                </div>
              </section>

              <section className="space-y-2 border-t border-border pt-5">
                <label
                  htmlFor="work-order-diagnostics"
                  className="text-sm font-semibold"
                >
                  Diagnostic notes
                </label>
                <textarea
                  id="work-order-diagnostics"
                  rows={5}
                  value={diagnosticNotes}
                  onChange={(event) => setDiagnosticNotes(event.target.value)}
                  placeholder="Record findings, recommended work, or handover context…"
                  className="w-full rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
                />
                <button
                  type="button"
                  onClick={handleDiagnostics}
                  disabled={updateDiagnostics.isPending}
                  className="rounded-lg border border-border px-3 py-2 text-xs font-semibold hover:bg-muted disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Save diagnostics
                </button>
              </section>

              {selected.id && <WorkOrderDocuments workOrderId={selected.id} />}
            </div>
          )}
        </section>
      </section>
    </div>
  )
}
