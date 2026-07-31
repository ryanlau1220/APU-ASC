import * as React from 'react'
import { useUserSession } from '../routes/__root'

interface RequireAuthProps {
  allowedRoles: string[]
  children: React.ReactNode
}

export function RequireAuth({ allowedRoles, children }: RequireAuthProps) {
  const { userSession, loading } = useUserSession()

  React.useEffect(() => {
    if (!loading && (!userSession || !userSession.authenticated)) {
      window.location.href = '/oauth2/authorization/keycloak'
    }
  }, [loading, userSession])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <div className="text-center space-y-3">
          <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-xs text-muted-foreground font-medium">
            Verifying permissions...
          </p>
        </div>
      </div>
    )
  }

  if (!userSession || !userSession.authenticated) {
    return null
  }

  const userRoles = userSession.roles || []
  const hasPermission = allowedRoles.some(
    (role) =>
      userRoles.includes(role) ||
      userRoles.includes(`ROLE_${role}`) ||
      userRoles.includes('SYSTEM_ADMIN') ||
      userRoles.includes('ROLE_SYSTEM_ADMIN'),
  )

  if (!hasPermission) {
    if (typeof window !== 'undefined') {
      window.location.href = '/403'
    }
    return null
  }

  return <>{children}</>
}
