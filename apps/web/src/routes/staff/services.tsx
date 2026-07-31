import { createFileRoute } from '@tanstack/react-router'
import { BookOpen, CheckCircle2, Clock } from 'lucide-react'
import { useGetServices } from '../../api/generated/endpoints'
import type { ServiceDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/staff/services')({
  component: StaffServicesContent,
})

function StaffServicesContent() {
  const { data: servicesData = [] } = useGetServices()
  const services = (servicesData || []) as ServiceDto[]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <BookOpen className="w-4 h-4" />
            Service Catalog & Pricing
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Counter Service Package Directory
          </h1>
          <p className="text-xs text-muted-foreground">
            Look up service packages, estimated labor duration, and package
            pricing for customer inquiries.
          </p>
        </section>

        {/* Services Grid */}
        {services.length === 0 ? (
          <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
            No service packages registered in catalog.
          </div>
        ) : (
          <section className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {services.map((s) => (
              <div
                key={s.id}
                className="bg-card border border-border rounded-xl p-5 space-y-4 shadow-sm"
              >
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-muted text-muted-foreground">
                    {s.id}
                  </span>
                  <span className="text-xs font-bold text-primary px-2.5 py-1 rounded bg-primary/10 border border-primary/30">
                    RM {Number(s.basePrice || 0).toFixed(2)}
                  </span>
                </div>

                <div>
                  <h3 className="font-heading text-base font-bold">{s.name}</h3>
                  <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                    {s.description ||
                      'Standard automotive maintenance package.'}
                  </p>
                </div>

                <div className="pt-3 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-primary" />
                    Duration: {s.durationMinutes || 60} mins
                  </span>
                  <span className="flex items-center gap-1 text-status-completed font-semibold">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Active
                  </span>
                </div>
              </div>
            ))}
          </section>
        )}
      </main>

      <Footer />
    </div>
  )
}
