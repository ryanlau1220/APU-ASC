import { createFileRoute } from '@tanstack/react-router'
import { Calendar, CheckCircle2, Clock, Plus } from 'lucide-react'
import * as React from 'react'
import {
  useCancelAppointment,
  useCreateAppointment,
  useGetMyAppointments,
  useGetMyVehicles,
  useGetServices,
  useGetSlotAvailability,
  useUpdateAppointment,
} from '../../api/generated/endpoints'
import type {
  AppointmentDto,
  ServiceDto,
  SlotAvailabilityDto,
  VehicleDto,
} from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/customer/appointments')({
  component: CustomerAppointmentsContent,
})

function CustomerAppointmentsContent() {
  const [selectedVehicleId, setSelectedVehicleId] = React.useState('')
  const [selectedServiceId, setSelectedServiceId] = React.useState('')
  const [appointmentDate, setAppointmentDate] = React.useState('')
  const [timeSlot, setTimeSlot] = React.useState('09:00 - 10:00 AM')
  const [notes, setNotes] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)
  const [reschedulingAppointment, setReschedulingAppointment] =
    React.useState<AppointmentDto | null>(null)
  const [rescheduleDate, setRescheduleDate] = React.useState('')
  const [rescheduleTimeSlot, setRescheduleTimeSlot] = React.useState('')

  const { data: vehiclesData = [] } = useGetMyVehicles()
  const myVehicles = (vehiclesData || []) as VehicleDto[]

  const { data: servicesData = [] } = useGetServices()
  const services = (servicesData || []) as ServiceDto[]

  const { data: appointmentsData = [], refetch } = useGetMyAppointments()
  const myAppointments = (appointmentsData || []) as AppointmentDto[]
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
  const {
    data: rescheduleAvailabilityData = [],
    isFetching: isLoadingRescheduleAvailability,
  } = useGetSlotAvailability(
    { date: rescheduleDate },
    { query: { enabled: Boolean(rescheduleDate) } },
  )
  const rescheduleAvailability =
    rescheduleAvailabilityData as Required<SlotAvailabilityDto>[]

  const createAppointmentMutation = useCreateAppointment({
    mutation: {
      onSuccess: () => {
        setMessage('Appointment booked successfully!')
        setSelectedVehicleId('')
        setSelectedServiceId('')
        setAppointmentDate('')
        setNotes('')
        refetch()
        refetchAvailability()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to book appointment.')
      },
    },
  })

  const cancelAppointmentMutation = useCancelAppointment({
    mutation: {
      onSuccess: () => {
        setMessage('Appointment cancelled successfully!')
        refetch()
        refetchAvailability()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to cancel appointment.')
      },
    },
  })
  const updateAppointmentMutation = useUpdateAppointment<Error>()

  const handleBook = (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedVehicleId || !selectedServiceId || !appointmentDate) {
      setMessage('Please select a vehicle, service package, and date.')
      return
    }
    if (
      !slotAvailability.find((slot) => slot.timeSlot === timeSlot)?.bookable
    ) {
      setMessage('Please choose a time slot with remaining workshop capacity.')
      return
    }
    setMessage(null)
    createAppointmentMutation.mutate({
      data: {
        vehicleId: selectedVehicleId,
        serviceId: selectedServiceId,
        appointmentDate,
        timeSlot,
        notes,
      },
    })
  }

  const startRescheduling = (appointment: AppointmentDto) => {
    setReschedulingAppointment(appointment)
    setRescheduleDate(appointment.appointmentDate || '')
    setRescheduleTimeSlot('')
    setMessage(null)
  }

  const rescheduleAppointment = (event: React.FormEvent) => {
    event.preventDefault()
    if (
      !reschedulingAppointment?.id ||
      !rescheduleDate ||
      !rescheduleTimeSlot
    ) {
      setMessage('Choose a new date and available time slot.')
      return
    }
    if (
      !rescheduleAvailability.find(
        (slot) => slot.timeSlot === rescheduleTimeSlot,
      )?.bookable
    ) {
      setMessage('Please choose a time slot with remaining workshop capacity.')
      return
    }
    updateAppointmentMutation.mutate(
      {
        id: reschedulingAppointment.id,
        data: {
          ...reschedulingAppointment,
          appointmentDate: rescheduleDate,
          timeSlot: rescheduleTimeSlot,
        },
      },
      {
        onSuccess: () => {
          setMessage('Appointment rescheduled successfully!')
          setReschedulingAppointment(null)
          refetch()
          refetchAvailability()
        },
        onError: (err) =>
          setMessage(err.message || 'Failed to reschedule appointment.'),
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
            Service Scheduling
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Book Maintenance & Track Appointments
          </h1>
          <p className="text-xs text-muted-foreground">
            Choose your vehicle, select an available service package and time
            slot, and track workshop status in real-time.
          </p>
        </section>

        {/* Appointment Booking Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Plus className="w-4 h-4 text-primary" />
            Book a New Appointment
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
            onSubmit={handleBook}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
          >
            <div>
              <label
                htmlFor="vehicleSelect"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Select Vehicle
              </label>
              <select
                id="vehicleSelect"
                required
                value={selectedVehicleId}
                onChange={(e) => setSelectedVehicleId(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              >
                <option value="">-- Choose Registered Vehicle --</option>
                {myVehicles.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.licensePlate} ({v.make} {v.model})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="serviceSelect"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Select Service Package
              </label>
              <select
                id="serviceSelect"
                required
                value={selectedServiceId}
                onChange={(e) => setSelectedServiceId(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              >
                <option value="">-- Choose Service Package --</option>
                {services.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} - RM {Number(s.basePrice || 0).toFixed(2)}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="appointmentDate"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Appointment Date
              </label>
              <input
                id="appointmentDate"
                type="date"
                required
                value={appointmentDate}
                onChange={(e) => setAppointmentDate(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="timeSlot"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Preferred Time Slot
              </label>
              <select
                id="timeSlot"
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
                htmlFor="notes"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Special Work Notes / Remarks
              </label>
              <input
                id="notes"
                type="text"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="e.g. Engine noise check, oil change"
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
                  ? 'Booking...'
                  : 'Confirm Appointment'}
              </button>
            </div>
          </form>
        </section>

        {/* Appointment History Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Clock className="w-4 h-4 text-primary" />
            My Appointment Schedule
          </h2>

          {reschedulingAppointment && (
            <form
              onSubmit={rescheduleAppointment}
              className="grid gap-3 rounded-lg border border-primary/30 bg-primary/5 p-4 sm:grid-cols-[1fr_1fr_auto]"
            >
              <label className="text-xs font-semibold text-muted-foreground">
                New appointment date
                <input
                  type="date"
                  min={new Date().toISOString().slice(0, 10)}
                  value={rescheduleDate}
                  onChange={(event) => {
                    setRescheduleDate(event.target.value)
                    setRescheduleTimeSlot('')
                  }}
                  required
                  className="mt-1 w-full rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary"
                />
              </label>
              <label className="text-xs font-semibold text-muted-foreground">
                Available time slot
                <select
                  value={rescheduleTimeSlot}
                  onChange={(event) =>
                    setRescheduleTimeSlot(event.target.value)
                  }
                  required
                  disabled={!rescheduleDate || isLoadingRescheduleAvailability}
                  className="mt-1 w-full rounded-lg border border-border bg-input px-3 py-2 text-xs outline-none focus:border-primary disabled:opacity-50"
                >
                  <option value="">
                    {isLoadingRescheduleAvailability
                      ? 'Checking capacity…'
                      : 'Choose an available slot'}
                  </option>
                  {rescheduleAvailability.map((slot) => (
                    <option
                      key={slot.timeSlot}
                      value={slot.timeSlot}
                      disabled={!slot.bookable}
                    >
                      {slot.timeSlot} — {slot.available} of {slot.capacity}{' '}
                      available{!slot.bookable ? ' (full)' : ''}
                    </option>
                  ))}
                </select>
              </label>
              <div className="flex items-end gap-2">
                <button
                  type="submit"
                  disabled={
                    updateAppointmentMutation.isPending ||
                    isLoadingRescheduleAvailability
                  }
                  className="rounded-lg bg-primary px-3 py-2 text-xs font-semibold text-primary-foreground hover:opacity-90 disabled:opacity-50"
                >
                  Save schedule
                </button>
                <button
                  type="button"
                  onClick={() => setReschedulingAppointment(null)}
                  className="rounded-lg border border-border px-3 py-2 text-xs font-semibold hover:bg-muted"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}

          {myAppointments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No appointments scheduled. Fill in the form above to book a
              workshop visit.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Booking ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service ID</th>
                    <th className="py-3 px-3">Scheduled Date</th>
                    <th className="py-3 px-3">Technician</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {myAppointments.map((apt) => (
                    <tr
                      key={apt.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {apt.id}
                      </td>
                      <td className="py-3.5 px-3 font-mono">{apt.vehicleId}</td>
                      <td className="py-3.5 px-3 font-medium">
                        {apt.serviceId}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {apt.appointmentDate} ({apt.timeSlot})
                      </td>
                      <td className="py-3.5 px-3">
                        {apt.technicianId || 'Unassigned'}
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
                        {apt.status === 'PENDING' ? (
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => startRescheduling(apt)}
                              className="rounded-lg border border-primary/40 px-3 py-1 text-xs font-semibold text-primary transition-colors hover:bg-primary/10"
                            >
                              Reschedule
                            </button>
                            <button
                              type="button"
                              onClick={() =>
                                apt.id &&
                                cancelAppointmentMutation.mutate({ id: apt.id })
                              }
                              disabled={cancelAppointmentMutation.isPending}
                              className="rounded-lg border border-destructive/40 px-3 py-1 text-xs font-semibold text-destructive transition-colors hover:bg-destructive/10 disabled:opacity-50"
                            >
                              Cancel
                            </button>
                          </div>
                        ) : (
                          <span className="text-[11px] text-muted-foreground">
                            —
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
