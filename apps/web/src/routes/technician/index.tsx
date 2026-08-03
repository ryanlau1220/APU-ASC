import { createFileRoute, Link } from '@tanstack/react-router'
import { Calendar, CheckCircle2, Clock, Wrench } from 'lucide-react'
import { useGetMyWorkOrders } from '../../api/workOrders'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/technician/')({
  component: TechnicianDashboardContent,
})

function TechnicianDashboardContent() {
  const { data: myJobs = [] } = useGetMyWorkOrders()

  const activeJobs = myJobs.filter((a) => a.status === 'IN_PROGRESS')
  const completedJobs = myJobs.filter((a) => a.status === 'COMPLETED')
  const pendingJobs = myJobs.filter(
    (a) => a.status === 'OPEN' || a.status === 'DIAGNOSING',
  )

  const metrics = [
    {
      title: 'In-Progress Jobs',
      value: String(activeJobs.length),
      subtitle: 'Currently servicing',
      icon: Wrench,
      color: 'text-primary bg-primary/10 border-primary/30',
    },
    {
      title: 'Pending Work Orders',
      value: String(pendingJobs.length),
      subtitle: 'Awaiting bay intake',
      icon: Calendar,
      color: 'text-secondary-foreground bg-secondary border-secondary/50',
    },
    {
      title: 'Completed Today',
      value: String(completedJobs.length),
      subtitle: 'Vehicles inspected',
      icon: CheckCircle2,
      color:
        'text-status-completed bg-status-completed/10 border-status-completed/30',
    },
  ]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/30 text-xs font-semibold text-primary">
              <Wrench className="w-3.5 h-3.5" />
              Workshop Bay Console
            </div>
            <h1 className="font-heading text-2xl sm:text-3xl font-bold tracking-tight">
              Technician Job Queue & Diagnostics
            </h1>
            <p className="text-sm text-muted-foreground">
              Touch-friendly workshop bay dashboard to manage active work
              orders, update job status, and record diagnostic notes.
            </p>
          </div>

          <Link
            to="/technician/jobs"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-semibold hover:opacity-90 transition-opacity"
          >
            <Wrench className="w-4 h-4" />
            Manage Work Orders
          </Link>
        </section>

        {/* Metrics Grid */}
        <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
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

        {/* Assigned Jobs Queue */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary" />
              <h2 className="font-heading text-lg font-bold">
                My Assigned Workshop Jobs
              </h2>
            </div>
            <Link
              to="/technician/jobs"
              className="text-xs font-semibold text-primary hover:underline"
            >
              View Work Orders
            </Link>
          </div>

          {myJobs.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No service jobs assigned to your technician queue.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Job ID</th>
                    <th className="py-3 px-3">Vehicle ID</th>
                    <th className="py-3 px-3">Service Package</th>
                    <th className="py-3 px-3">Opened</th>
                    <th className="py-3 px-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {myJobs.slice(0, 5).map((workOrder) => (
                    <tr
                      key={workOrder.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {workOrder.id}
                      </td>
                      <td className="py-3.5 px-3 font-mono">
                        {workOrder.vehicleId}
                      </td>
                      <td className="py-3.5 px-3 font-medium">
                        {workOrder.serviceId}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {workOrder.openedAt
                          ? new Date(workOrder.openedAt).toLocaleDateString()
                          : 'Just opened'}
                      </td>
                      <td className="py-3.5 px-3">
                        <span
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border ${
                            workOrder.status === 'IN_PROGRESS'
                              ? 'text-status-confirmed border-status-confirmed/30 bg-status-confirmed/10'
                              : workOrder.status === 'COMPLETED'
                                ? 'text-status-completed border-status-completed/30 bg-status-completed/10'
                                : 'text-status-pending border-status-pending/30 bg-status-pending/10'
                          }`}
                        >
                          <CheckCircle2 className="w-3 h-3" />
                          {workOrder.status}
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
