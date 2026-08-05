import { ShieldCheck } from 'lucide-react'
import { useUserSession } from '../routes/__root'

export default function Footer() {
  const { userSession } = useUserSession()
  const roles = userSession?.roles ?? []

  const canManageIdentity =
    roles.includes('MANAGER') || roles.includes('SYSTEM_ADMIN')

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

        {canManageIdentity && (
          <div className="flex flex-wrap items-center gap-4">
            <a
              href="/auth/admin/"
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
