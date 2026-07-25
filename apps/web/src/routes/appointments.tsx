import { createFileRoute } from '@tanstack/react-router'
import { Calendar, CheckCircle2, Clock, Filter, Wrench } from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/appointments')({
  component: AppointmentsPage,
})

function AppointmentsPage() {
  const [activeFilter, setActiveFilter] = React.useState<string>('ALL')

  const appointments = [
    {
      id: 'APT-1001',
      customer: 'Alex Tan (USR-101)',
      vehicle: 'WXD 8821 (Honda Civic 2022)',
      service: 'Full Engine Synthetic Oil Service',
      appointmentDate: '2026-07-26',
      timeSlot: '10:00 AM - 11:30 AM',
      technician: 'Master Tech Rahman',
      status: 'CONFIRMED',
      notes: 'Customer requested synthetic 5W-30 engine oil.',
    },
    {
      id: 'APT-1002',
      customer: 'Siti Aminah (USR-102)',
      vehicle: 'VCE 4512 (Perodua Myvi 2021)',
      service: 'Brake Disc & Pad Replacement',
      appointmentDate: '2026-07-26',
      timeSlot: '02:00 PM - 03:30 PM',
      technician: 'Unassigned',
      status: 'PENDING',
      notes: 'Front brake squeaking sound during stopping.',
    },
    {
      id: 'APT-1003',
      customer: 'Devon Lee (USR-103)',
      vehicle: 'BQA 9010 (Toyota Camry 2023)',
      service: 'Air Conditioning Maintenance & Gas Refill',
      appointmentDate: '2026-07-25',
      timeSlot: '04:00 PM - 05:00 PM',
      technician: 'Tech Kevin Wong',
      status: 'COMPLETED',
      notes: 'Cooling gas pressure restored and leak test passed.',
    },
  ]

  const filteredAppointments =
    activeFilter === 'ALL'
      ? appointments
      : appointments.filter((a) => a.status === activeFilter)

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <Calendar className="w-4 h-4" />
              Appointment Scheduling
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Workshop Service Bookings
            </h1>
            <p className="text-xs text-muted-foreground">
              Track customer booking slots, assigned workshop technicians, and
              service progress.
            </p>
          </div>
        </section>

        {/* Filters */}
        <section className="flex items-center gap-2 overflow-x-auto pb-2">
          <Filter className="w-4 h-4 text-muted-foreground mr-1 shrink-0" />
          {['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED'].map((status) => (
            <button
              key={status}
              type="button"
              onClick={() => setActiveFilter(status)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors border ${
                activeFilter === status
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-card text-muted-foreground border-border hover:text-foreground'
              }`}
            >
              {status}
            </button>
          ))}
        </section>

        {/* Appointments List */}
        <section className="space-y-4">
          {filteredAppointments.map((apt) => (
            <div
              key={apt.id}
              className="bg-card border border-border rounded-xl p-5 space-y-4"
            >
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border pb-3">
                <div className="flex items-center gap-3">
                  <span className="text-xs font-mono font-bold px-2.5 py-1 rounded bg-muted text-foreground">
                    {apt.id}
                  </span>
                  <span className="text-xs font-semibold text-muted-foreground">
                    {apt.appointmentDate}
                  </span>
                </div>

                <span
                  className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border ${
                    apt.status === 'CONFIRMED'
                      ? 'text-status-confirmed border-status-confirmed/30 bg-status-confirmed/10'
                      : apt.status === 'COMPLETED'
                        ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                        : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                  }`}
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  {apt.status}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
                <div className="space-y-1">
                  <span className="text-muted-foreground font-medium block">
                    Customer & Vehicle
                  </span>
                  <div className="font-semibold text-foreground">
                    {apt.customer}
                  </div>
                  <div className="text-muted-foreground">{apt.vehicle}</div>
                </div>

                <div className="space-y-1">
                  <span className="text-muted-foreground font-medium block">
                    Service & Slot
                  </span>
                  <div className="font-semibold text-foreground">
                    {apt.service}
                  </div>
                  <div className="flex items-center gap-1 text-primary">
                    <Clock className="w-3.5 h-3.5" />
                    {apt.timeSlot}
                  </div>
                </div>

                <div className="space-y-1">
                  <span className="text-muted-foreground font-medium block">
                    Assigned Technician
                  </span>
                  <div className="flex items-center gap-1.5 font-semibold text-foreground">
                    <Wrench className="w-3.5 h-3.5 text-primary" />
                    {apt.technician}
                  </div>
                  <p className="text-[11px] text-muted-foreground italic mt-1">
                    "{apt.notes}"
                  </p>
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
