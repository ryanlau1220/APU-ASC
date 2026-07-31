import { createFileRoute, Link } from '@tanstack/react-router'
import {
  ArrowUpRight,
  BookOpen,
  Calendar,
  Car,
  CheckCircle2,
  Clock,
  CreditCard,
  FileCode,
  Plus,
  ShieldCheck,
} from 'lucide-react'
import * as React from 'react'
import {
  useGetAllAppointments,
  useGetAllPayments,
  useGetAllVehicles,
  useGetServices,
} from '../api/generated/endpoints'
import type {
  AppointmentDto,
  PaymentDto,
  ServiceDto,
  VehicleDto,
} from '../api/generated/models'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { useUserSession } from './__root'

export const Route = createFileRoute('/')({ component: DashboardPage })

function DashboardPage() {
  const { userSession, loading } = useUserSession()
  const isAuthenticated = userSession?.authenticated ?? false
  const roles = userSession?.roles ?? []

  const isStaffOrManager =
    roles.includes('MANAGER') ||
    roles.includes('STAFF') ||
    roles.includes('WORKSHOP_MANAGER') ||
    roles.includes('SYSTEM_ADMIN') ||
    roles.includes('ROLE_MANAGER') ||
    roles.includes('ROLE_STAFF') ||
    roles.includes('ROLE_WORKSHOP_MANAGER') ||
    roles.includes('ROLE_SYSTEM_ADMIN')

  const { data: appointmentsData = [] } = useGetAllAppointments({
    query: {
      enabled: isAuthenticated && isStaffOrManager,
      retry: false,
    },
  })
  const appointments = (appointmentsData || []) as AppointmentDto[]

  const { data: servicesData = [] } = useGetServices()
  const services = (servicesData || []) as ServiceDto[]

  const { data: vehiclesData = [] } = useGetAllVehicles({
    query: {
      enabled: isAuthenticated && isStaffOrManager,
      retry: false,
    },
  })
  const vehicles = (vehiclesData || []) as VehicleDto[]

  const { data: paymentsData = [] } = useGetAllPayments({
    query: {
      enabled: isAuthenticated && isStaffOrManager,
      retry: false,
    },
  })
  const payments = (paymentsData || []) as PaymentDto[]

  React.useEffect(() => {
    if (!loading) {
      if (!isAuthenticated) {
        window.location.href = '/oauth2/authorization/keycloak'
      } else {
        // Forward authenticated users to manager portal
        window.location.href = '/manager'
      }
    }
  }, [loading, isAuthenticated])

  if (loading || !isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <div className="text-center space-y-3">
          <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-xs text-muted-foreground font-medium">
            Authenticating & connecting to APU-ASC portal...
          </p>
        </div>
      </div>
    )
  }

  const totalRevenue = payments
    .filter((p) => p.paymentStatus === 'PAID')
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)

  const metrics = [
    {
      title: 'Active Appointments',
      value: String(appointments.length),
      subtitle: `${appointments.filter((a) => a.status === 'PENDING').length} pending approval`,
      icon: Calendar,
      color: 'text-primary bg-primary/10 border-primary/30',
    },
    {
      title: 'Catalog Services',
      value: String(services.length),
      subtitle: 'Active packages',
      icon: BookOpen,
      color: 'text-secondary-foreground bg-secondary border-secondary/50',
    },
    {
      title: 'Registered Vehicles',
      value: String(vehicles.length),
      subtitle: 'Customer fleet',
      icon: Car,
      color: 'text-accent-foreground bg-accent border-accent/50',
    },
    {
      title: 'Total Revenue',
      value: `RM ${totalRevenue.toFixed(2)}`,
      subtitle: 'Completed payments',
      icon: CreditCard,
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
              <ShieldCheck className="w-3.5 h-3.5" />
              Automotive Operations Command Centre
            </div>
            <h1 className="font-heading text-2xl sm:text-3xl font-bold tracking-tight">
              Service Management & Scheduling Overview
            </h1>
            <p className="text-sm text-muted-foreground">
              Monitor active workshop appointments, manage vehicle service
              catalogs, track customer billing, and review diagnostic feedback.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/manager/appointments"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-semibold hover:opacity-90 transition-opacity"
            >
              <Plus className="w-4 h-4" />
              Book Appointment
            </Link>
            <a
              href="http://localhost:8081/scalar"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-border bg-muted hover:border-primary/40 text-sm font-semibold transition-colors"
            >
              <FileCode className="w-4 h-4 text-primary" />
              Scalar API Docs
            </a>
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

        {/* Recent Appointments Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary" />
              <h2 className="font-heading text-lg font-bold">
                Upcoming Service Appointments
              </h2>
            </div>
            <Link
              to="/manager/appointments"
              className="text-xs font-semibold text-primary hover:underline inline-flex items-center gap-1"
            >
              View All <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {appointments.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No service appointments recorded yet. Click "Book Appointment" to
              create one.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Customer ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service ID</th>
                    <th className="py-3 px-3">Date & Slot</th>
                    <th className="py-3 px-3">Technician ID</th>
                    <th className="py-3 px-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {appointments.slice(0, 5).map((apt) => (
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
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {apt.vehicleId}
                      </td>
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
