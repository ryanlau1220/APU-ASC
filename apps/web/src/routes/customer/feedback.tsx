import { createFileRoute } from '@tanstack/react-router'
import { MessageSquare, Star } from 'lucide-react'
import * as React from 'react'
import {
  useGetMyAppointments,
  useGetMyFeedback,
  useSubmitFeedback,
} from '../../api/generated/endpoints'
import type { AppointmentDto, FeedbackDto } from '../../api/generated/models'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { useUserSession } from '../__root'

export const Route = createFileRoute('/customer/feedback')({
  component: CustomerFeedbackContent,
})

function CustomerFeedbackContent() {
  const { userSession } = useUserSession()
  const customerId =
    userSession?.id || userSession?.keycloakId || 'USR-CUSTOMER'

  const [appointmentId, setAppointmentId] = React.useState('')
  const [rating, setRating] = React.useState(5)
  const [comments, setComments] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const { data: appointmentsData = [] } = useGetMyAppointments()
  const completedAppointments = (
    (appointmentsData || []) as AppointmentDto[]
  ).filter((a) => a.status === 'COMPLETED')

  const { data: myFeedbackData = [], refetch: refetchFeedback } =
    useGetMyFeedback()
  const myFeedbacks = (myFeedbackData || []) as FeedbackDto[]

  const submitFeedbackMutation = useSubmitFeedback({
    mutation: {
      onSuccess: () => {
        setMessage('Thank you! Your service feedback has been submitted.')
        setAppointmentId('')
        setRating(5)
        setComments('')
        refetchFeedback()
      },
      onError: (err: Error) => {
        setMessage(err.message || 'Failed to submit feedback.')
      },
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!appointmentId || !comments) {
      setMessage('Please select an appointment and enter feedback comments.')
      return
    }
    setMessage(null)
    submitFeedbackMutation.mutate({
      data: {
        appointmentId,
        customerId,
        rating,
        comments,
      },
    })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <MessageSquare className="w-4 h-4" />
            Service Experience & Reviews
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Submit Workshop Service Feedback
          </h1>
          <p className="text-xs text-muted-foreground">
            Share your feedback and rate your experience with our workshop
            technicians and service quality.
          </p>
        </section>

        {/* Feedback Form */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4 max-w-2xl">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Star className="w-4 h-4 text-primary" />
            Rate Recent Service
          </h2>

          {message && (
            <div
              className={`p-3 rounded-lg text-xs font-semibold ${
                message.includes('Thank you')
                  ? 'bg-status-completed/10 text-status-completed border border-status-completed/30'
                  : 'bg-destructive/10 text-destructive border border-destructive/30'
              }`}
            >
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label
                htmlFor="aptSelect"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Completed Appointment
              </label>
              <select
                id="aptSelect"
                required
                value={appointmentId}
                onChange={(e) => setAppointmentId(e.target.value)}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              >
                <option value="">-- Select Completed Appointment --</option>
                {completedAppointments.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.id} - {a.appointmentDate} ({a.serviceId})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="ratingSelect"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Overall Service Rating (1 to 5 Stars)
              </label>
              <select
                id="ratingSelect"
                value={rating}
                onChange={(e) => setRating(Number(e.target.value))}
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors font-semibold text-primary"
              >
                <option value={5}>⭐⭐⭐⭐⭐ (5/5 Excellent)</option>
                <option value={4}>⭐⭐⭐⭐ (4/5 Very Good)</option>
                <option value={3}>⭐⭐⭐ (3/5 Satisfactory)</option>
                <option value={2}>⭐⭐ (2/5 Poor)</option>
                <option value={1}>⭐ (1/5 Very Poor)</option>
              </select>
            </div>

            <div>
              <label
                htmlFor="commentsText"
                className="block text-xs font-semibold text-muted-foreground mb-1"
              >
                Feedback Comments & Diagnostics Review
              </label>
              <textarea
                id="commentsText"
                rows={4}
                required
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                placeholder="Describe your service experience, technician professionalism, and vehicle condition..."
                className="w-full px-3 py-2 bg-input border border-border rounded-lg text-xs outline-none focus:border-primary transition-colors"
              />
            </div>

            <button
              type="submit"
              disabled={submitFeedbackMutation.isPending}
              className="py-2.5 px-6 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity disabled:opacity-50"
            >
              {submitFeedbackMutation.isPending
                ? 'Submitting...'
                : 'Submit Service Feedback'}
            </button>
          </form>
        </section>

        {/* My Submitted Feedback History */}
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-4">
          <h2 className="font-heading text-lg font-bold flex items-center gap-2">
            <Star className="w-5 h-5 text-amber-500 fill-amber-500" />
            My Submitted Feedback & Reviews ({myFeedbacks.length})
          </h2>

          {myFeedbacks.length === 0 ? (
            <div className="text-center py-6 text-xs text-muted-foreground">
              You have not submitted any service feedback yet.
            </div>
          ) : (
            <div className="space-y-3">
              {myFeedbacks.map((fb) => (
                <div
                  key={fb.id}
                  className="p-4 rounded-xl bg-muted/40 border border-border space-y-2 text-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-foreground">
                      Appointment #{fb.appointmentId}
                    </span>
                    <span className="font-bold text-amber-500 flex items-center gap-1">
                      {'⭐'.repeat(fb.rating || 5)} ({fb.rating}/5)
                    </span>
                  </div>
                  <p className="text-muted-foreground">{fb.comments}</p>
                  {fb.createdAt && (
                    <div className="text-[10px] text-muted-foreground">
                      Submitted on {new Date(fb.createdAt).toLocaleDateString()}
                    </div>
                  )}
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
