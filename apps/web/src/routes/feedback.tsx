import { useQuery } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import { MessageSquare, Star, User, Wrench } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/feedback')({ component: FeedbackPage })

interface FeedbackDto {
  id: string
  appointmentId: string
  customerId: string
  technicianId: string
  rating: number
  comments: string
  technicianDiagnosticNotes: string
  createdAt: string
}

function FeedbackPage() {
  const { data: reviews = [], isLoading } = useQuery<FeedbackDto[]>({
    queryKey: ['feedbacks'],
    queryFn: () => bffFetch<FeedbackDto[]>('/api/v1/feedbacks'),
  })

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
        {isLoading ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Loading service feedback...
          </div>
        ) : reviews.length === 0 ? (
          <div className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground">
            No customer reviews or diagnostic notes recorded yet.
          </div>
        ) : (
          <section className="space-y-4">
            {reviews.map((r) => (
              <div
                key={r.id}
                className="bg-card border border-border rounded-xl p-5 space-y-4"
              >
                <div className="flex items-center justify-between border-b border-border pb-3">
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4 text-primary" />
                    <span className="font-semibold text-xs">
                      Customer ID: {r.customerId}
                    </span>
                  </div>

                  <div className="flex items-center gap-1 text-primary">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <Star
                        // biome-ignore lint/suspicious/noArrayIndexKey: rating 5 stars
                        key={i}
                        className={`w-4 h-4 ${
                          i < (r.rating || 0)
                            ? 'fill-primary text-primary'
                            : 'text-muted border-border'
                        }`}
                      />
                    ))}
                  </div>
                </div>

                <div className="space-y-2 text-xs">
                  {r.comments && (
                    <p className="font-medium text-foreground text-sm">
                      "{r.comments}"
                    </p>
                  )}
                  {r.technicianDiagnosticNotes && (
                    <div className="bg-muted p-3 rounded-lg border border-border space-y-1">
                      <div className="flex items-center gap-1.5 text-primary font-semibold text-[11px]">
                        <Wrench className="w-3.5 h-3.5" />
                        Technician Diagnostic Notes (ID:{' '}
                        {r.technicianId || 'Unassigned'})
                      </div>
                      <p className="text-muted-foreground">
                        {r.technicianDiagnosticNotes}
                      </p>
                    </div>
                  )}
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
