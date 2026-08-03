import { createFileRoute } from '@tanstack/react-router'
import { Calendar, CheckCircle2, Plus, UserCheck } from 'lucide-react'
import * as React from 'react'
import {
  useCreateAppointment,
  useCreateWorkOrder,
  useGetAllAppointments,
  useGetSlotAvailability,
  useUpdateAppointmentStatus,
} from '../../api/generated/endpoints'
import type {
  AppointmentDto,
  SlotAvailabilityDto,
  UpdateAppointmentStatusStatus,
} from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { appointmentTransitionTargets } from '../../lib/lifecycle'

export const Route = createFileRoute('/staff/appointments')({
  component: StaffAppointmentsContent,
})

function StaffAppointmentsContent() {
  const [customerId, setCustomerId] = React.useState('')
  const [vehicleId, setVehicleId] = React.useState('')
  const [serviceId, setServiceId] = React.useState('')
  const [appointmentDate, setAppointmentDate] = React.useState('')
  const [timeSlot, setTimeSlot] = React.useState('09:00 - 10:00 AM')
  const [notes, setNotes] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)
  const [technicianAssignments, setTechnicianAssignments] = React.useState<
    Record<string, string>
  >({})

  const { data: appointmentsData = [], refetch } = useGetAllAppointments()
  const appointments = (appointmentsData || []) as AppointmentDto[]
  const {
    data: slotAvailabilityData = [],
    isFetching: isLoadingAvailability,
    refetch: refetchAvailability,
  } = useGetSlotAvailability(
    { date: appointmentDate },
    { query: { enabled: Boolean(appointmentDate) } },
  )
  const slotAvailability =
    slotAvailabilityData as Required<SlotAvailabilityDto>[]

  const createAppointmentMutation = useCreateAppointment({
    mutation: {
      onSuccess: () => {
        setMessage('Walk-in appointment created successfully!')
        setCustomerId('')
        setVehicleId('')
        setServiceId('')
        setAppointmentDate('')
        setNotes('')
        refetch()
        refetchAvailability()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to create appointment.')
      },
    },
  })

  const updateStatusMutation = useUpdateAppointmentStatus({
    mutation: {
      onSuccess: () => {
        setMessage('Appointment status updated!')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to update status.')
      },
    },
  })
  const createWorkOrderMutation = useCreateWorkOrder<Error>()

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    if (!customerId || !vehicleId || !serviceId || !appointmentDate) {
      setMessage(
        'Please provide customer, vehicle, service, and appointment date.',
      )
      return
    }
    if (
      !slotAvailability.find((slot) => slot.timeSlot === timeSlot)?.bookable
    ) {
      setMessage('Please select a time slot with remaining workshop capacity.')
      return
    }
    setMessage(null)
    createAppointmentMutation.mutate({
      data: {
        customerId,
        vehicleId,
        serviceId,
        appointmentDate,
        timeSlot,
        notes,
      },
    })
  }

  const handleStatusChange = (id: string, status: string) => {
    setMessage(null)
    updateStatusMutation.mutate({
      id,
      params: { status: status as UpdateAppointmentStatusStatus },
    })
  }

  const handleOpenWorkOrder = (appointmentId: string) => {
    createWorkOrderMutation.mutate(
      {
        data: {
          appointmentId,
          technicianId: technicianAssignments[appointmentId],
        },
      },
      {
        onSuccess: () => setMessage('Work order opened successfully!'),
        onError: (err: Error) => {
          setMessage(err.message || 'Failed to open work order.')
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
            <Calendar className="w-4 h-4" />
            Counter Intake & Scheduling
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Customer Intake & Appointment Management
          </h1>
          <p className="text-xs text-muted-foreground">
            Register walk-in customers, confirm booking attendance, and open a
            separate work order when the vehicle enters the workshop.
          </p>
        </section>

        {/* Walk-in Booking Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Plus className="w-4 h-4 text-primary" />
            Register Walk-in Customer Appointment
          </h2>

          {message && (
            <div
              className={`p-3 rounded-lg text-xs font-semibold ${
                message.includes('successfully') || message.includes('updated')
                  ? 'bg-status-completed/10 text-status-completed border border-status-completed/30'
                  : 'bg-destructive/10 text-destructive border border-destructive/30'
              }`}
            >
              {message}
            </div>
          )}

          <form
            onSubmit={handleCreate}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
          >
            <div>
              <label
                htmlFor="custInput"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Customer ID / Email
              </label>
              <input
                id="custInput"
                type="text"
                required
                value={customerId}
                onChange={(e) => setCustomerId(e.target.value)}
                placeholder="e.g. USR-401"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="vehInput"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Vehicle ID / License Plate
              </label>
              <input
                id="vehInput"
                type="text"
                required
                value={vehicleId}
                onChange={(e) => setVehicleId(e.target.value)}
                placeholder="e.g. VEH-101 / WYY 8888"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="svcInput"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Service Package ID
              </label>
              <input
                id="svcInput"
                type="text"
                required
                value={serviceId}
                onChange={(e) => setServiceId(e.target.value)}
                placeholder="e.g. SVC-ENGINE-OIL"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="aptDate"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Scheduled Date
              </label>
              <input
                id="aptDate"
                type="date"
                required
                value={appointmentDate}
                onChange={(e) => setAppointmentDate(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="timeSlotSelect"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Time Slot
              </label>
              <select
                id="timeSlotSelect"
                value={timeSlot}
                onChange={(e) => setTimeSlot(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              >
                {!appointmentDate && (
                  <option value="09:00 - 10:00 AM">Select a date first</option>
                )}
                {slotAvailability.map((slot) => (
                  <option
                    key={slot.timeSlot}
                    value={slot.timeSlot}
                    disabled={!slot.bookable}
                  >
                    {slot.timeSlot} — {slot.available} of {slot.capacity}{' '}
                    available
                    {!slot.bookable ? ' (full)' : ''}
                  </option>
                ))}
              </select>
              {appointmentDate && isLoadingAvailability && (
                <p className="mt-1 text-[11px] text-muted-foreground">
                  Checking workshop capacity…
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="notesInput"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Special Remarks
              </label>
              <input
                id="notesInput"
                type="text"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Walk-in check-in remarks"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="sm:col-span-2 lg:col-span-1 flex items-end">
              <button
                type="submit"
                disabled={
                  createAppointmentMutation.isPending ||
                  isLoadingAvailability ||
                  !slotAvailability.find((slot) => slot.timeSlot === timeSlot)
                    ?.bookable
                }
                className="w-full py-2 px-4 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {createAppointmentMutation.isPending
                  ? 'Creating...'
                  : 'Register Walk-in Booking'}
              </button>
            </div>
          </form>
        </section>

        {/* Master Appointment Management Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <UserCheck className="w-4 h-4 text-primary" />
            Active Service Appointments
          </h2>

          {appointments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No appointments registered.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Customer ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service</th>
                    <th className="py-3 px-3">Date & Slot</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3">Booking Action</th>
                    <th className="py-3 px-3">Technician ID</th>
                    <th className="py-3 px-3">Workshop Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {appointments.map((apt) => (
                    <tr
                      key={apt.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {apt.id}
                      </td>
                      <td className="py-3.5 px-3 font-medium">
                        {apt.customerId}
                      </td>
                      <td className="py-3.5 px-3 font-mono">{apt.vehicleId}</td>
                      <td className="py-3.5 px-3 font-medium">
                        {apt.serviceId}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {apt.appointmentDate} ({apt.timeSlot})
                      </td>
                      <td className="py-3.5 px-3">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border ${
                            apt.status === 'CONFIRMED'
                              ? 'text-status-confirmed border-status-confirmed/30 bg-status-confirmed/10'
                              : apt.status === 'COMPLETED'
                                ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                                : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                          }`}
                        >
                          <CheckCircle2 className="w-3 h-3" />
                          {apt.status}
                        </span>
                      </td>
                      <td className="py-3.5 px-3">
                        {appointmentTransitionTargets(apt.status).length > 0 ? (
                          <select
                            value={apt.status}
                            onChange={(e) =>
                              apt.id &&
                              handleStatusChange(apt.id, e.target.value)
                            }
                            className="px-2 py-1 bg-input border border-border rounded text-[11px] outline-none focus:border-primary"
                          >
                            <option value={apt.status} disabled>
                              {apt.status}
                            </option>
                            {appointmentTransitionTargets(apt.status).map(
                              (status) => (
                                <option key={status} value={status}>
                                  {status}
                                </option>
                              ),
                            )}
                          </select>
                        ) : (
                          <span className="text-[11px] text-muted-foreground">
                            Final state
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-3">
                        <input
                          type="text"
                          value={technicianAssignments[apt.id || ''] || ''}
                          onChange={(event) =>
                            apt.id &&
                            setTechnicianAssignments((current) => ({
                              ...current,
                              [apt.id as string]: event.target.value,
                            }))
                          }
                          placeholder="Optional technician ID"
                          className="w-36 rounded border border-border bg-input px-2 py-1 text-[11px] outline-none focus:border-primary"
                        />
                      </td>
                      <td className="py-3.5 px-3">
                        {apt.status === 'CONFIRMED' ? (
                          <button
                            type="button"
                            onClick={() =>
                              apt.id && handleOpenWorkOrder(apt.id)
                            }
                            disabled={createWorkOrderMutation.isPending}
                            className="rounded-lg border border-primary/40 px-3 py-1 text-[11px] font-semibold text-primary hover:bg-primary/10 disabled:opacity-50"
                          >
                            Open Work Order
                          </button>
                        ) : (
                          <span className="text-[11px] text-muted-foreground">
                            {apt.status === 'PENDING'
                              ? 'Confirm booking first'
                              : '—'}
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
