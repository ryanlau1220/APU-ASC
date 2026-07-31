import { createFileRoute } from '@tanstack/react-router'
import { Calendar, Car, Plus, ShieldCheck, User } from 'lucide-react'
import * as React from 'react'
import {
  useCreateVehicle,
  useGetAllVehicles,
} from '../../api/generated/endpoints'
import type { VehicleDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { useUserSession } from '../__root'

export const Route = createFileRoute('/customer/vehicles')({
  component: CustomerVehiclesContent,
})

function CustomerVehiclesContent() {
  const { userSession } = useUserSession()
  const customerId =
    userSession?.id || userSession?.keycloakId || 'USR-CUSTOMER'

  const [licensePlate, setLicensePlate] = React.useState('')
  const [make, setMake] = React.useState('')
  const [model, setModel] = React.useState('')
  const [yearOfManufacture, setYearOfManufacture] = React.useState(2023)
  const [vin, setVin] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: vehiclesData = [], refetch } = useGetAllVehicles()
  const myVehicles = ((vehiclesData || []) as VehicleDto[]).filter(
    (v) => v.customerId === customerId || !customerId,
  )

  const createVehicleMutation = useCreateVehicle({
    mutation: {
      onSuccess: () => {
        setMessage('Vehicle registered successfully!')
        setLicensePlate('')
        setMake('')
        setModel('')
        setVin('')
        refetch()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to register vehicle.')
      },
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setMessage(null)
    createVehicleMutation.mutate({
      data: {
        customerId,
        licensePlate,
        make,
        model,
        yearOfManufacture: Number(yearOfManufacture),
      },
    })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <Car className="w-4 h-4" />
            My Garage
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Vehicle Fleet Registry
          </h1>
          <p className="text-xs text-muted-foreground">
            Register your vehicle details to easily schedule maintenance
            appointments and track service records.
          </p>
        </section>

        {/* Vehicle Registration Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Plus className="w-4 h-4 text-primary" />
            Register New Vehicle
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
            onSubmit={handleSubmit}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
          >
            <div>
              <label
                htmlFor="licensePlate"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                License Plate Number
              </label>
              <input
                id="licensePlate"
                type="text"
                required
                value={licensePlate}
                onChange={(e) => setLicensePlate(e.target.value)}
                placeholder="e.g. VAB 1234"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="make"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Vehicle Make / Brand
              </label>
              <input
                id="make"
                type="text"
                required
                value={make}
                onChange={(e) => setMake(e.target.value)}
                placeholder="e.g. Toyota / Honda"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="model"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Model Name
              </label>
              <input
                id="model"
                type="text"
                required
                value={model}
                onChange={(e) => setModel(e.target.value)}
                placeholder="e.g. Camry / Civic"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="yearOfManufacture"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Year of Manufacture
              </label>
              <input
                id="yearOfManufacture"
                type="number"
                required
                min={1990}
                max={2027}
                value={yearOfManufacture}
                onChange={(e) => setYearOfManufacture(Number(e.target.value))}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="vin"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                VIN / Chassis Number (Optional)
              </label>
              <input
                id="vin"
                type="text"
                value={vin}
                onChange={(e) => setVin(e.target.value)}
                placeholder="Optional VIN"
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <div className="sm:col-span-2 lg:col-span-1 flex items-end">
              <button
                type="submit"
                disabled={createVehicleMutation.isPending}
                className="w-full py-2 px-4 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {createVehicleMutation.isPending
                  ? 'Registering...'
                  : 'Register Vehicle'}
              </button>
            </div>
          </form>
        </section>

        {/* Registered Vehicles List */}
        <section className="space-y-4">
          <h2 className="font-heading text-lg font-bold">
            My Registered Vehicles
          </h2>

          {myVehicles.length === 0 ? (
            <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
              No vehicles registered under your account yet. Use the form above
              to add your vehicle.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {myVehicles.map((v) => (
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
                      <span>Manufactured Year: {v.yearOfManufacture}</span>
                    </div>
                  </div>

                  <div className="pt-3 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <User className="w-3.5 h-3.5" />
                      Owner Registered
                    </span>
                    <ShieldCheck className="w-4 h-4 text-status-completed" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  )
}
