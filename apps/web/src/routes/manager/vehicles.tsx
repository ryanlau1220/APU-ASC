import { createFileRoute } from '@tanstack/react-router'
import { Calendar, Car, ShieldCheck, User } from 'lucide-react'
import { useGetAllVehicles } from '../../api/generated/endpoints'
import type { VehicleDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/manager/vehicles')({
  component: ManagerVehiclesContent,
})

function ManagerVehiclesContent() {
  const { data: vehiclesData = [] } = useGetAllVehicles()
  const vehicles = (vehiclesData || []) as VehicleDto[]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <Car className="w-4 h-4" />
            Master Fleet Directory
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Customer Fleet & Vehicle Administration
          </h1>
          <p className="text-xs text-muted-foreground">
            Master database of all customer vehicles registered across APU-ASC
            workshop branches.
          </p>
        </section>

        {/* Vehicles Grid */}
        {vehicles.length === 0 ? (
          <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
            No registered vehicles found in system database.
          </div>
        ) : (
          <section className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {vehicles.map((v) => (
              <div
                key={v.id}
                className="bg-card border border-border rounded-xl p-5 space-y-4 shadow-sm"
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
                    <span>Year: {v.yearOfManufacture}</span>
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
