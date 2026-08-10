import { describe, expect, it } from 'vitest'
import { getRoleHomeRoute, getRoleNavItems } from './Header'

describe('Role-Based Header Navigation Unit Tests', () => {
  describe('getRoleHomeRoute', () => {
    it('should return /manager/ for MANAGER and SYSTEM_ADMIN roles', () => {
      expect(getRoleHomeRoute(['MANAGER'])).toBe('/manager/')
      expect(getRoleHomeRoute(['SYSTEM_ADMIN'])).toBe('/manager/')
    })

    it('should return /staff/ for STAFF role', () => {
      expect(getRoleHomeRoute(['STAFF'])).toBe('/staff/')
      expect(getRoleHomeRoute(['ROLE_STAFF'])).toBe('/staff/')
    })

    it('should return /technician/ for TECHNICIAN role', () => {
      expect(getRoleHomeRoute(['TECHNICIAN'])).toBe('/technician/')
      expect(getRoleHomeRoute(['ROLE_TECHNICIAN'])).toBe('/technician/')
    })

    it('should default to /customer/ for CUSTOMER role or empty roles', () => {
      expect(getRoleHomeRoute(['CUSTOMER'])).toBe('/customer/')
      expect(getRoleHomeRoute([])).toBe('/customer/')
    })
  })

  describe('getRoleNavItems', () => {
    it('should return Customer specific links for CUSTOMER with Vehicles as 2nd item', () => {
      const items = getRoleNavItems(['CUSTOMER'])
      const routes = items.map((i) => i.to)

      expect(routes[0]).toBe('/customer/')
      expect(routes[1]).toBe('/customer/vehicles')
      expect(routes[2]).toBe('/customer/appointments')
      expect(routes).toContain('/customer/payments')
      expect(routes).toContain('/customer/feedback')

      // Should NOT contain internal manager or staff routes
      expect(routes).not.toContain('/manager/services')
      expect(routes).not.toContain('/staff/users')
    })

    it('should return Manager specific links for MANAGER with Service Catalog as 2nd item', () => {
      const items = getRoleNavItems(['MANAGER'])
      const routes = items.map((i) => i.to)

      expect(routes[0]).toBe('/manager/')
      expect(routes[1]).toBe('/manager/services')
      expect(routes).toContain('/manager/appointments')
      expect(routes).toContain('/manager/work-orders')
      expect(routes).toContain('/manager/vehicles')
      expect(routes).toContain('/manager/operations')

      // Should NOT contain customer feedback or staff intake
      expect(routes).not.toContain('/customer/feedback')
      expect(routes).not.toContain('/staff/users')
    })

    it('should return Staff specific links for STAFF with Services as 2nd item', () => {
      const items = getRoleNavItems(['STAFF'])
      const routes = items.map((i) => i.to)

      expect(routes[0]).toBe('/staff/')
      expect(routes[1]).toBe('/staff/services')
      expect(routes).toContain('/staff/appointments')
      expect(routes).toContain('/staff/work-orders')
      expect(routes).toContain('/staff/users')
      expect(routes).toContain('/staff/payments')
    })

    it('should return Technician specific links for TECHNICIAN', () => {
      const items = getRoleNavItems(['TECHNICIAN'])
      const routes = items.map((i) => i.to)

      expect(routes[0]).toBe('/technician/')
      expect(routes[1]).toBe('/technician/jobs')
      expect(routes[2]).toBe('/technician/feedback')
    })
  })
})
