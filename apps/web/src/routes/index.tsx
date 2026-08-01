import { createFileRoute, Navigate } from '@tanstack/react-router'
import { getRoleHomeRoute } from '../components/Header'
import { useUserSession } from './__root'

export const Route = createFileRoute('/')({ component: DashboardPage })

function DashboardPage() {
  const { userSession, loading } = useUserSession()
  const isAuthenticated = userSession?.authenticated ?? false
  const roles = userSession?.roles ?? []

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <div className="text-center space-y-3">
          <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-xs text-muted-foreground font-medium">
            Authenticating & connecting to APU-ASC portal...
          </p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    window.location.href = '/oauth2/authorization/keycloak'
    return null
  }

  const targetHome = getRoleHomeRoute(roles)
  return <Navigate to={targetHome as never} replace />
}
