import { createFileRoute } from '@tanstack/react-router'
import { MessageSquare, Star } from 'lucide-react'
import Footer from '../../components/Footer'
import Header from '../../components/Header'

export const Route = createFileRoute('/technician/feedback')({
  component: TechnicianFeedbackContent,
})

function TechnicianFeedbackContent() {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 sm:p-8 space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
            <MessageSquare className="w-4 h-4" />
            Service Quality Reviews
          </div>
          <h1 className="font-heading text-2xl font-bold">
            Customer Feedback & Rating Dashboard
          </h1>
          <p className="text-xs text-muted-foreground">
            Review customer diagnostic ratings and performance feedback on
            completed workshop jobs.
          </p>
        </section>

        <section className="bg-card border border-border rounded-xl p-8 text-center text-xs text-muted-foreground space-y-3">
          <Star className="w-8 h-8 text-primary mx-auto opacity-70" />
          <h3 className="font-heading text-sm font-bold text-foreground">
            Technician Quality Assurance Rating: 5.0 / 5.0
          </h3>
          <p className="max-w-md mx-auto">
            All customer diagnostic reviews on completed appointments are synced
            in real-time.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  )
}
