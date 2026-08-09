import { describe, expect, it } from 'vitest'

interface UserSession {
  authenticated: boolean
  roles: string[]
}

function checkHasPermission(
  userSession: UserSession | null,
  allowedRoles: string[],
): boolean {
  if (!userSession || !userSession.authenticated) return false
  const userRoles = userSession.roles || []
  return allowedRoles.some(
    (role) =>
      userRoles.includes(role) ||
      userRoles.includes(`ROLE_${role}`) ||
      userRoles.includes('MANAGER') ||
      userRoles.includes('ROLE_MANAGER') ||
      userRoles.includes('SYSTEM_ADMIN') ||
      userRoles.includes('ROLE_SYSTEM_ADMIN'),
  )
}

describe('Role-Based Access Control (RequireAuth Guard)', () => {
  it('should deny unauthenticated users', () => {
    const session: UserSession = { authenticated: false, roles: [] }
    expect(checkHasPermission(session, ['MANAGER'])).toBe(false)
  })

  it('should grant access to users matching allowed roles', () => {
    const managerSession: UserSession = {
      authenticated: true,
      roles: ['MANAGER'],
    }
    expect(checkHasPermission(managerSession, ['MANAGER'])).toBe(true)

    const customerSession: UserSession = {
      authenticated: true,
      roles: ['CUSTOMER'],
    }
    expect(checkHasPermission(customerSession, ['CUSTOMER'])).toBe(true)
  })

  it('should deny access when user role is not in allowedRoles', () => {
    const customerSession: UserSession = {
      authenticated: true,
      roles: ['CUSTOMER'],
    }
    expect(checkHasPermission(customerSession, ['MANAGER'])).toBe(false)
  })

  it('should allow SYSTEM_ADMIN full access across all portals', () => {
    const adminSession: UserSession = {
      authenticated: true,
      roles: ['SYSTEM_ADMIN'],
    }
    expect(checkHasPermission(adminSession, ['MANAGER'])).toBe(true)
    expect(checkHasPermission(adminSession, ['STAFF'])).toBe(true)
    expect(checkHasPermission(adminSession, ['TECHNICIAN'])).toBe(true)
    expect(checkHasPermission(adminSession, ['CUSTOMER'])).toBe(true)
  })

  it('should allow MANAGER multi-portal access for the executive portal switcher', () => {
    const managerSession: UserSession = {
      authenticated: true,
      roles: ['MANAGER'],
    }

    expect(checkHasPermission(managerSession, ['CUSTOMER'])).toBe(true)
    expect(checkHasPermission(managerSession, ['STAFF'])).toBe(true)
    expect(checkHasPermission(managerSession, ['TECHNICIAN'])).toBe(true)
  })
})
