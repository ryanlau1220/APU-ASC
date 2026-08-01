import { createFileRoute, Link } from '@tanstack/react-router'
import {
  Calendar,
  Car,
  CheckCircle2,
  Clock,
  CreditCard,
  Plus,
  User,
} from 'lucide-react'
import {
  useGetMyAppointments,
  useGetMyPayments,
  useGetMyVehicles,
} from '../../api/generated/endpoints'
import type {
  AppointmentDto,
  PaymentDto,
  VehicleDto,
} from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { useUserSession } from '../__root'

export const Route = createFileRoute('/customer/')({
  component: CustomerDashboardContent,
})

function CustomerDashboardContent() {
  const { userSession } = useUserSession()

  const { data: vehiclesData = [] } = useGetMyVehicles()
  const myVehicles = (vehiclesData || []) as VehicleDto[]

  const { data: appointmentsData = [] } = useGetMyAppointments()
  const myAppointments = (appointmentsData || []) as AppointmentDto[]

  const { data: paymentsData = [] } = useGetMyPayments()
  const myPayments = (paymentsData || []) as PaymentDto[]

  const pendingPayments = myPayments.filter((p) => p.paymentStatus !== 'PAID')

  const metrics = [
    {
      title: 'My Vehicles',
      value: String(myVehicles.length),
      subtitle: 'Registered fleet',
      icon: Car,
      color: 'text-primary bg-primary/10 border-primary/30',
    },
    {
      title: 'Active Bookings',
      value: String(
        myAppointments.filter((a) => a.status !== 'COMPLETED').length,
      ),
      subtitle: 'Pending & in-progress',
      icon: Calendar,
      color: 'text-secondary-foreground bg-secondary border-secondary/50',
    },
    {
      title: 'Pending Bills',
      value: String(pendingPayments.length),
      subtitle: 'Unpaid invoices',
      icon: CreditCard,
      color: 'text-accent-foreground bg-accent border-accent/50',
    },
    {
      title: 'Completed Services',
      value: String(
        myAppointments.filter((a) => a.status === 'COMPLETED').length,
      ),
      subtitle: 'Serviced appointments',
      icon: CheckCircle2,
      color:
        'text-status-completed bg-status-completed/10 border-status-completed/30',
    },
  ]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Banner Section */}
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative overflow-hidden">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/30 text-xs font-semibold text-primary">
              <User className="w-3.5 h-3.5" />
              Customer Self-Service Portal
            </div>
            <h1 className="font-heading text-2xl sm:text-3xl font-bold tracking-tight">
              Welcome Back,{' '}
              {userSession?.fullName ||
                userSession?.username ||
                'Valued Customer'}
            </h1>
            <p className="text-sm text-muted-foreground">
              Manage your registered vehicles, book service slots, track
              workshop progress, and pay invoices online.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/customer/appointments"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-semibold hover:opacity-90 transition-opacity"
            >
              <Plus className="w-4 h-4" />
              Book New Service
            </Link>
            <Link
              to="/customer/vehicles"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-border bg-muted hover:border-primary/40 text-sm font-semibold transition-colors"
            >
              <Car className="w-4 h-4 text-primary" />
              Register Vehicle
            </Link>
          </div>
        </section>

        {/* Metrics Grid */}
        <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {metrics.map((m) => {
            const Icon = m.icon
            return (
              <div
                key={m.title}
                className="bg-card border border-border rounded-xl p-5 flex flex-col justify-between space-y-3"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-medium text-muted-foreground">
                    {m.title}
                  </span>
                  <div className={`p-2 rounded-lg border ${m.color}`}>
                    <Icon className="w-4 h-4" />
                  </div>
                </div>
                <div>
                  <div className="font-heading text-2xl font-bold">
                    {m.value}
                  </div>
                  <div className="text-[11px] text-muted-foreground mt-0.5">
                    {m.subtitle}
                  </div>
                </div>
              </div>
            )
          })}
        </section>

        {/* Recent Appointments */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary" />
              <h2 className="font-heading text-lg font-bold">
                My Service Bookings
              </h2>
            </div>
            <Link
              to="/customer/appointments"
              className="text-xs font-semibold text-primary hover:underline"
            >
              View All Appointments
            </Link>
          </div>

          {myAppointments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              You have no active appointments. Click "Book New Service" to
              schedule a maintenance slot.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Booking ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service Package</th>
                    <th className="py-3 px-3">Date & Slot</th>
                    <th className="py-3 px-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {myAppointments.slice(0, 5).map((apt) => (
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
