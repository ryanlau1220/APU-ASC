import { CheckCircle2, FileCode, ShieldCheck } from 'lucide-react'
import { useUserSession } from '../routes/__root'

export default function Footer() {
  const { userSession } = useUserSession()
  const roles = userSession?.roles ?? []

  const isInternalUser =
    roles.includes('MANAGER') ||
    roles.includes('STAFF') ||
    roles.includes('SYSTEM_ADMIN')

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

        {isInternalUser && (
          <div className="flex flex-wrap items-center gap-4">
            <a
              href="http://localhost:8081/"
              target="_blank"
              rel="noreferrer"
              className="flex items-center gap-1.5 text-status-active hover:underline font-medium transition-colors"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              Spring Boot API Online
            </a>
            <span className="text-border">|</span>
            <a
              href="http://localhost:8081/scalar"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 hover:text-foreground transition-colors font-medium"
            >
              <FileCode className="w-3.5 h-3.5 text-primary" />
              Scalar Docs
            </a>
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
        )}
      </div>
    </footer>
  )
}
