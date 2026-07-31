import { createFileRoute, Link } from '@tanstack/react-router'
import { ShieldAlert, ShieldCheck } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/403')({ component: AccessDeniedPage })

function AccessDeniedPage() {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors justify-between">
      <Header />

      <main className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md bg-card border border-border rounded-xl p-6 sm:p-8 space-y-6 text-center shadow-lg">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-destructive/10 border border-destructive/30 text-destructive mb-2">
            <ShieldAlert className="w-6 h-6" />
          </div>
          <h1 className="font-heading text-2xl font-bold">
            403 - Access Denied
          </h1>
          <p className="text-xs text-muted-foreground leading-relaxed">
            You do not have the required role permissions to view this portal
            area. If you believe this is an error, please contact your APU-ASC
            workshop administrator.
          </p>

          <Link
            to="/"
            className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground font-semibold hover:opacity-90 transition-opacity flex items-center justify-center gap-2 text-xs shadow-md"
          >
            <ShieldCheck className="w-4 h-4" />
            Return to Authorized Portal
          </Link>
        </div>
      </main>

      <Footer />
    </div>
  )
}
