import { createFileRoute } from '@tanstack/react-router'
import { Calendar, Car, ShieldCheck, User } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/vehicles')({ component: VehiclesPage })

function VehiclesPage() {
  const vehicles = [
    {
      id: 'VEC-901',
      customerId: 'USR-101 (Alex Tan)',
      licensePlate: 'WXD 8821',
      make: 'Honda',
      model: 'Civic Turbo',
      year: 2022,
      createdAt: '2026-01-15',
    },
    {
      id: 'VEC-902',
      customerId: 'USR-102 (Siti Aminah)',
      licensePlate: 'VCE 4512',
      make: 'Perodua',
      model: 'Myvi AV',
      year: 2021,
      createdAt: '2026-02-10',
    },
    {
      id: 'VEC-903',
      customerId: 'USR-103 (Devon Lee)',
      licensePlate: 'BQA 9010',
      make: 'Toyota',
      model: 'Camry Hybrid',
      year: 2023,
      createdAt: '2026-03-04',
    },
  ]

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
                  <span>Manufactured Year: {v.year}</span>
                </div>
              </div>

              <div className="pt-3 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  <User className="w-3.5 h-3.5" />
                  {v.customerId}
                </span>
                <ShieldCheck className="w-4 h-4 text-status-completed" />
              </div>
            </div>
          ))}
        </section>
      </main>

      <Footer />
    </div>
  )
}
