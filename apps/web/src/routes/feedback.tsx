import { createFileRoute } from '@tanstack/react-router'
import { MessageSquare, Star, User, Wrench } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/feedback')({ component: FeedbackPage })

function FeedbackPage() {
  const reviews = [
    {
      id: 'FBK-401',
      appointmentId: 'APT-1003',
      customer: 'Devon Lee',
      technician: 'Tech Kevin Wong',
      rating: 5,
      comments:
        'Excellent AC cooling repair! Service was completed faster than estimated duration.',
      diagnosticNotes:
        'Replaced AC filter and pressurized system to 35 PSI with zero leaks.',
      date: '2026-07-25',
    },
    {
      id: 'FBK-402',
      appointmentId: 'APT-1000',
      customer: 'Sarah Jenkins',
      technician: 'Master Tech Rahman',
      rating: 4,
      comments:
        'Very professional synthetic oil change. Brake check report was very thorough.',
      diagnosticNotes:
        'Engine oil level verified at maximum line. Front brake pads at 85% thickness.',
      date: '2026-07-24',
    },
  ]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <MessageSquare className="w-4 h-4" />
              Customer Feedback & Diagnostics
            </div>
            <h1 className="font-heading text-2xl font-bold">
              Service Reviews & Notes
            </h1>
            <p className="text-xs text-muted-foreground">
              Review customer service ratings and technician diagnostic repair
              notes.
            </p>
          </div>
        </section>

        {/* Reviews List */}
        <section className="space-y-4">
          {reviews.map((r) => (
            <div
              key={r.id}
              className="bg-card border border-border rounded-xl p-5 space-y-4"
            >
              <div className="flex items-center justify-between border-b border-border pb-3">
                <div className="flex items-center gap-2">
                  <User className="w-4 h-4 text-primary" />
                  <span className="font-semibold text-xs">{r.customer}</span>
                </div>

                <div className="flex items-center gap-1 text-primary">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      // biome-ignore lint/suspicious/noArrayIndexKey: fixed 5-star rating list
                      key={i}
                      className={`w-4 h-4 ${
                        i < r.rating
                          ? 'fill-primary text-primary'
                          : 'text-muted border-border'
                      }`}
                    />
                  ))}
                </div>
              </div>

              <div className="space-y-2 text-xs">
                <p className="font-medium text-foreground text-sm">
                  "{r.comments}"
                </p>
                <div className="bg-muted p-3 rounded-lg border border-border space-y-1">
                  <div className="flex items-center gap-1.5 text-primary font-semibold text-[11px]">
                    <Wrench className="w-3.5 h-3.5" />
                    Technician Diagnostic Notes ({r.technician})
                  </div>
                  <p className="text-muted-foreground">{r.diagnosticNotes}</p>
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
