import { useQuery } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import { Calendar, Car, ShieldCheck, User } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/vehicles')({ component: VehiclesPage })

interface VehicleDto {
  id: string
  customerId: string
  licensePlate: string
  make: string
  model: string
  yearOfManufacture: number
  createdAt: string
}

function VehiclesPage() {
  const { data: vehicles = [], isLoading } = useQuery<VehicleDto[]>({
    queryKey: ['vehicles'],
    queryFn: () => bffFetch<VehicleDto[]>('/api/v1/vehicles'),
  })

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <Car className="w-4 h-4" />
              Vehicle Fleet Registry
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Customer Vehicles
            </h1>
            <p className="text-xs text-muted-foreground">
              Manage registered customer license plates, vehicle makes, models,
              and manufacture years.
            </p>
          </div>
        </section>

        {/* Vehicles Grid */}
        {isLoading ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Loading vehicle fleet...
          </div>
        ) : vehicles.length === 0 ? (
          <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
            No registered vehicles found in database. Register customer vehicles
            to display fleet details.
          </div>
        ) : (
          <section className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {vehicles.map((v) => (
              <div
                key={v.id}
                className="bg-card border border-border rounded-xl p-5 space-y-4"
              >
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-muted text-muted-foreground">
                    {v.id}
                  </span>
                  <span className="text-xs font-mono font-bold text-primary px-2.5 py-1 rounded bg-primary/10 border border-primary/30">
                    {v.licensePlate}
                  </span>
                </div>

                <div>
                  <h3 className="font-heading text-base font-bold">
                    {v.make} {v.model}
                  </h3>
                  <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
                    <Calendar className="w-3.5 h-3.5 text-primary" />
                    <span>Manufactured Year: {v.yearOfManufacture}</span>
                  </div>
                </div>

                <div className="pt-3 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <User className="w-3.5 h-3.5" />
                    Customer: {v.customerId}
                  </span>
                  <ShieldCheck className="w-4 h-4 text-status-completed" />
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
