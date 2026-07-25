import { createFileRoute } from '@tanstack/react-router'
import { BookOpen, CheckCircle2, Clock, Filter, Tag } from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/services')({ component: ServicesPage })

function ServicesPage() {
  const [activeCategory, setActiveCategory] = React.useState<string>('ALL')

  const categories = [
    { id: 'ALL', name: 'All Services' },
    { id: 'MAINTENANCE', name: 'Regular Maintenance' },
    { id: 'REPAIR', name: 'Brake & Mechanical Repair' },
    { id: 'DIAGNOSTIC', name: 'Air-Con & Electrical' },
  ]

  const services = [
    {
      id: 'SVC-101',
      category: 'MAINTENANCE',
      name: 'Full Engine Synthetic Oil Service',
      description:
        'Includes 100% synthetic engine oil replacement, oil filter change, and 21-point safety inspection.',
      durationMinutes: 60,
      basePrice: 180.0,
      status: 'ACTIVE',
    },
    {
      id: 'SVC-102',
      category: 'REPAIR',
      name: 'Brake Disc & Pad Replacement',
      description:
        'Replacement of front/rear ceramic brake pads and rotor resurfacing for high stopping power.',
      durationMinutes: 90,
      basePrice: 320.0,
      status: 'ACTIVE',
    },
    {
      id: 'SVC-103',
      category: 'DIAGNOSTIC',
      name: 'Air Conditioning Maintenance & Gas Refill',
      description:
        'Full AC system flush, leak check, compressor oil top-up, and R134a refrigerant refill.',
      durationMinutes: 60,
      basePrice: 150.0,
      status: 'ACTIVE',
    },
    {
      id: 'SVC-104',
      category: 'MAINTENANCE',
      name: 'Transmission Fluid & Filter Flush',
      description:
        'Automatic transmission fluid exchange and filter cleaning to ensure smooth gear shifts.',
      durationMinutes: 75,
      basePrice: 240.0,
      status: 'ACTIVE',
    },
  ]

  const filteredServices =
    activeCategory === 'ALL'
      ? services
      : services.filter((s) => s.category === activeCategory)

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <BookOpen className="w-4 h-4" />
              Service Catalog
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Automotive Service Offerings
            </h1>
            <p className="text-xs text-muted-foreground">
              Browse standard servicing packages, fixed prices, estimated
              durations, and repair diagnostics.
            </p>
          </div>
        </section>

        {/* Category Filters */}
        <section className="flex items-center gap-2 overflow-x-auto pb-2">
          <Filter className="w-4 h-4 text-muted-foreground mr-1 shrink-0" />
          {categories.map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => setActiveCategory(cat.id)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors border ${
                activeCategory === cat.id
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-card text-muted-foreground border-border hover:text-foreground'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </section>

        {/* Service Cards Grid */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredServices.map((svc) => (
            <div
              key={svc.id}
              className="bg-card border border-border rounded-xl p-5 flex flex-col justify-between space-y-4"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-mono font-bold tracking-wider px-2 py-0.5 rounded bg-muted text-muted-foreground">
                    {svc.id}
                  </span>
                  <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-status-completed">
                    <CheckCircle2 className="w-3 h-3" />
                    {svc.status}
                  </span>
                </div>
                <h3 className="font-heading text-base font-bold">{svc.name}</h3>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {svc.description}
                </p>
              </div>

              <div className="pt-3 border-t border-border flex items-center justify-between text-xs">
                <div className="flex items-center gap-4 text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-primary" />
                    {svc.durationMinutes} mins
                  </span>
                  <span className="flex items-center gap-1">
                    <Tag className="w-3.5 h-3.5 text-primary" />
                    {svc.category}
                  </span>
                </div>
                <div className="font-heading text-lg font-bold text-foreground">
                  RM {svc.basePrice.toFixed(2)}
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
