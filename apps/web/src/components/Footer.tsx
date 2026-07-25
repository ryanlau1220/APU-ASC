import { CheckCircle2, ShieldCheck } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="border-t border-border bg-card py-6 mt-auto transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-muted-foreground">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-primary" />
          <span>
            &copy; {new Date().getFullYear()} APU Automotive Service Centre. All
            rights reserved.
          </span>
        </div>

        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5 text-status-active">
            <CheckCircle2 className="w-3.5 h-3.5" />
            Spring Boot 3.3 API Online
          </span>
          <span className="text-border">|</span>
          <a
            href="http://localhost/auth/admin/"
            target="_blank"
            rel="noreferrer"
            className="hover:text-foreground transition-colors"
          >
            Keycloak Console
          </a>
        </div>
      </div>
    </footer>
  )
}
