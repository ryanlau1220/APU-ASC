import { createFileRoute } from '@tanstack/react-router'
import { Calendar, CheckCircle2, Clock, Trash2 } from 'lucide-react'
import * as React from 'react'
import {
  useDeleteAppointment,
  useGetAllAppointments,
  useUpdateAppointmentStatus,
} from '../../api/generated/endpoints'
import type { AppointmentDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { appointmentTransitionTargets } from '../../lib/lifecycle'

export const Route = createFileRoute('/manager/appointments')({
  component: ManagerAppointmentsContent,
})

function ManagerAppointmentsContent() {
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: appointmentsData = [], refetch } = useGetAllAppointments()
  const appointments = (appointmentsData || []) as AppointmentDto[]

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

  const deleteAppointmentMutation = useDeleteAppointment({
    mutation: {
      onSuccess: () => {
        setMessage('Appointment cancelled successfully!')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to cancel appointment.')
      },
    },
  })

  const handleStatusChange = (id: string, status: string) => {
    setMessage(null)
    updateStatusMutation.mutate({
      id,
      params: { status },
    })
  }

  const handleDelete = (id: string) => {
    if (confirm(`Are you sure you want to cancel appointment ${id}?`)) {
      setMessage(null)
      deleteAppointmentMutation.mutate({ id })
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <Calendar className="w-4 h-4" />
            Master Schedule Control
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Executive Appointment Management
          </h1>
          <p className="text-xs text-muted-foreground">
            Monitor and manage booking attendance. Technician assignment and
            execution are tracked on separate work orders.
          </p>
        </section>

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

        {/* Master Schedule Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Clock className="w-4 h-4 text-primary" />
            Master Workshop Schedule
          </h2>

          {appointments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No service appointments recorded.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Booking ID</th>
                    <th className="py-3 px-3">Customer ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service Package</th>
                    <th className="py-3 px-3">Date & Slot</th>
                    <th className="py-3 px-3">Status</th>
                    <th className="py-3 px-3">Status Control</th>
                    <th className="py-3 px-3">Cancel</th>
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
                        <button
                          type="button"
                          onClick={() => apt.id && handleDelete(apt.id)}
                          className="p-1.5 rounded-lg text-destructive hover:bg-destructive/10 transition-colors"
                          title="Cancel Booking"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
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
