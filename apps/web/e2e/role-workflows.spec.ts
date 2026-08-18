import { expect, test, type Page, type Route } from '@playwright/test'

type ApiHandler = (route: Route, url: URL) => Promise<boolean>

const customerSession = {
  authenticated: true,
  id: 'USR-E2E-CUSTOMER',
  username: 'customer.e2e',
  fullName: 'E2E Customer',
  roles: ['CUSTOMER'],
}

const staffSession = {
  authenticated: true,
  id: 'USR-E2E-STAFF',
  username: 'staff.e2e',
  fullName: 'E2E Staff',
  roles: ['STAFF'],
}

const technicianSession = {
  authenticated: true,
  id: 'USR-E2E-TECHNICIAN',
  username: 'technician.e2e',
  fullName: 'E2E Technician',
  roles: ['TECHNICIAN'],
}

async function fulfillJson(route: Route, body: unknown) {
  await route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify(body),
  })
}

async function mockAuthenticatedApi(
  page: Page,
  session: typeof customerSession,
  handle: ApiHandler,
) {
  await page.route('**/api/v1/**', async (route) => {
    const url = new URL(route.request().url())

    if (url.pathname === '/api/v1/auth/me') {
      await fulfillJson(route, session)
      return
    }
    if (url.pathname === '/api/v1/notifications/unread-count') {
      await fulfillJson(route, { count: 0 })
      return
    }
    if (url.pathname === '/api/v1/events/stream') {
      await route.fulfill({
        contentType: 'text/event-stream',
        headers: { 'cache-control': 'no-cache' },
        body: ': connected\n\n',
      })
      return
    }
    if (await handle(route, url)) return

    await fulfillJson(route, [])
  })
}

test.describe('role-based operational journeys', () => {
  test('customer books an appointment with an available slot', async ({ page }) => {
    let submittedAppointment: Record<string, unknown> | undefined
    let appointments: Array<Record<string, unknown>> = []

    await mockAuthenticatedApi(page, customerSession, async (route, url) => {
      if (url.pathname === '/api/v1/vehicles/my') {
        await fulfillJson(route, [
          {
            id: 'VEH-E2E-1',
            licensePlate: 'E2E 1234',
            make: 'Toyota',
            model: 'Camry',
            year: 2024,
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/catalog/services') {
        await fulfillJson(route, [
          { id: 'SVC-E2E-1', name: 'E2E Oil Service', basePrice: 180 },
        ])
        return true
      }
      if (url.pathname === '/api/v1/scheduling/availability') {
        await fulfillJson(route, [
          {
            timeSlot: '09:00 - 10:00 AM',
            capacity: 2,
            booked: 0,
            available: 2,
            bookable: true,
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/appointments/my') {
        if (route.request().method() === 'POST') {
          submittedAppointment = route.request().postDataJSON()
          appointments = [
            {
              id: 'APT-E2E-1',
              ...submittedAppointment,
              status: 'PENDING',
            },
          ]
          await fulfillJson(route, appointments[0])
        } else {
          await fulfillJson(route, appointments)
        }
        return true
      }
      if (url.pathname === '/api/v1/appointments') {
        submittedAppointment = route.request().postDataJSON()
        appointments = [
          {
            id: 'APT-E2E-1',
            ...submittedAppointment,
            status: 'PENDING',
          },
        ]
        await fulfillJson(route, appointments[0])
        return true
      }
      if (url.pathname === '/api/v1/work-orders/my') {
        await fulfillJson(route, [])
        return true
      }
      return false
    })

    await page.goto('/customer/appointments')
    await expect(
      page.getByRole('heading', { name: 'Book Maintenance & Track Appointments' }),
    ).toBeVisible()

    await page.getByLabel('Select Vehicle').selectOption('VEH-E2E-1')
    await page
      .getByLabel('Select Service Package')
      .selectOption('SVC-E2E-1')
    await page.getByLabel('Appointment Date').fill('2026-12-15')
    await page.getByLabel('Preferred Time Slot').selectOption('09:00 - 10:00 AM')
    await page
      .getByLabel('Special Work Notes / Remarks')
      .fill('Please inspect the brake pads.')
    await page.getByRole('button', { name: 'Confirm Appointment' }).click()

    await expect(page.getByText('Appointment booked successfully!')).toBeVisible()
    expect(submittedAppointment).toEqual({
      vehicleId: 'VEH-E2E-1',
      serviceId: 'SVC-E2E-1',
      appointmentDate: '2026-12-15',
      timeSlot: '09:00 - 10:00 AM',
      notes: 'Please inspect the brake pads.',
    })
  })

  test('staff assigns a technician and begins diagnosis', async ({ page }) => {
    let workOrder = {
      id: 'WO-E2E-1',
      customerId: 'USR-E2E-CUSTOMER',
      vehicleId: 'VEH-E2E-1',
      serviceId: 'SVC-E2E-1',
      technicianId: undefined as string | undefined,
      status: 'OPEN',
      intakeNotes: 'Brake noise reported.',
      diagnosticNotes: '',
    }

    await mockAuthenticatedApi(page, staffSession, async (route, url) => {
      if (url.pathname === '/api/v1/work-orders') {
        await fulfillJson(route, [workOrder])
        return true
      }
      if (url.pathname === '/api/v1/users') {
        await fulfillJson(route, [
          {
            id: 'USR-E2E-TECHNICIAN',
            username: 'technician.e2e',
            fullName: 'E2E Technician',
            role: 'TECHNICIAN',
            status: 'ACTIVE',
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1') {
        if (route.request().method() === 'PUT') {
          workOrder = { ...workOrder, ...route.request().postDataJSON() }
        }
        await fulfillJson(route, workOrder)
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1/status') {
        workOrder = { ...workOrder, status: url.searchParams.get('status') || 'OPEN' }
        await fulfillJson(route, workOrder)
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1/documents') {
        await fulfillJson(route, [])
        return true
      }
      return false
    })

    await page.goto('/staff/work-orders')
    await expect(
      page.getByRole('heading', { name: 'Work-order control centre' }),
    ).toBeVisible()

    await page
      .getByLabel('Assigned technician')
      .selectOption('USR-E2E-TECHNICIAN')
    await page.getByRole('button', { name: 'Assign', exact: true }).click()
    await expect(page.getByText('Technician assignment saved.')).toBeVisible()
    expect(workOrder.technicianId).toBe('USR-E2E-TECHNICIAN')

    await page.getByRole('button', { name: 'Begin diagnosis' }).click()
    await expect(page.getByText('Work-order status updated.')).toBeVisible()
    expect(workOrder.status).toBe('DIAGNOSING')
  })

  test('technician records diagnostics and progresses an assigned job', async ({
    page,
  }) => {
    let workOrder = {
      id: 'WO-E2E-1',
      customerId: 'USR-E2E-CUSTOMER',
      vehicleId: 'VEH-E2E-1',
      serviceId: 'SVC-E2E-1',
      technicianId: 'USR-E2E-TECHNICIAN',
      status: 'OPEN',
      diagnosticNotes: '',
    }

    await mockAuthenticatedApi(page, technicianSession, async (route, url) => {
      if (url.pathname === '/api/v1/work-orders/my') {
        await fulfillJson(route, [workOrder])
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1/diagnostic-notes') {
        workOrder = {
          ...workOrder,
          diagnosticNotes: JSON.parse(route.request().postData() || '""'),
        }
        await fulfillJson(route, workOrder)
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1/status') {
        workOrder = { ...workOrder, status: url.searchParams.get('status') || 'OPEN' }
        await fulfillJson(route, workOrder)
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-1/documents') {
        await fulfillJson(route, [])
        return true
      }
      return false
    })

    await page.goto('/technician/jobs')
    await expect(
      page.getByRole('heading', { name: 'Bay Job Management & Progress Tracker' }),
    ).toBeVisible()

    await page
      .getByLabel('Diagnostic notes')
      .fill('Brake pads are worn and need replacement.')
    await page.getByRole('button', { name: 'Save diagnostics' }).click()
    await expect(
      page.getByText('Diagnostic notes saved successfully!'),
    ).toBeVisible()
    expect(workOrder.diagnosticNotes).toBe(
      'Brake pads are worn and need replacement.',
    )

    await page.getByRole('button', { name: 'Begin diagnosis' }).click()
    await expect(
      page.getByText('Work order status updated successfully!'),
    ).toBeVisible()
    expect(workOrder.status).toBe('DIAGNOSING')
  })

  test('customer can reschedule and cancel a pending appointment', async ({
    page,
  }) => {
    let appointments: Array<Record<string, unknown>> = [
      {
        id: 'APT-E2E-2',
        vehicleId: 'VEH-E2E-1',
        serviceId: 'SVC-E2E-1',
        appointmentDate: '2026-12-15',
        timeSlot: '09:00 - 10:00 AM',
        status: 'PENDING',
      },
    ]

    await mockAuthenticatedApi(page, customerSession, async (route, url) => {
      if (
        url.pathname === '/api/v1/vehicles/my' ||
        url.pathname === '/api/v1/catalog/services'
      ) {
        await fulfillJson(route, [])
        return true
      }
      if (url.pathname === '/api/v1/appointments/my') {
        await fulfillJson(route, appointments)
        return true
      }
      if (url.pathname === '/api/v1/work-orders/my') {
        await fulfillJson(route, [])
        return true
      }
      if (url.pathname === '/api/v1/scheduling/availability') {
        await fulfillJson(route, [
          {
            timeSlot: '02:00 - 03:00 PM',
            capacity: 2,
            booked: 0,
            available: 2,
            bookable: true,
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/appointments/APT-E2E-2') {
        if (route.request().method() === 'PUT') {
          appointments = [route.request().postDataJSON()]
          await fulfillJson(route, appointments[0])
        } else {
          await fulfillJson(route, appointments[0])
        }
        return true
      }
      if (url.pathname === '/api/v1/appointments/APT-E2E-2/cancel') {
        appointments = [{ ...appointments[0], status: 'CANCELLED' }]
        await fulfillJson(route, appointments[0])
        return true
      }
      return false
    })

    await page.goto('/customer/appointments')
    await page.getByRole('button', { name: 'Reschedule' }).click()
    await page.getByLabel('New appointment date').fill('2026-12-16')
    await page
      .getByLabel('Available time slot')
      .selectOption('02:00 - 03:00 PM')
    await page.getByRole('button', { name: 'Save schedule' }).click()

    await expect(
      page.getByText('Appointment rescheduled successfully!'),
    ).toBeVisible()
    expect(appointments[0]).toMatchObject({
      appointmentDate: '2026-12-16',
      timeSlot: '02:00 - 03:00 PM',
    })

    await page.getByRole('button', { name: 'Cancel', exact: true }).click()
    await expect(
      page.getByText('Appointment cancelled successfully!'),
    ).toBeVisible()
    expect(appointments[0]).toMatchObject({ status: 'CANCELLED' })
    await expect(page.getByText('CANCELLED', { exact: true })).toBeVisible()
  })

  test('customer approves a quotation before work begins', async ({ page }) => {
    let quotation = {
      id: 'QT-E2E-1',
      quoteNumber: 'QT-E2E-1',
      revision: 1,
      workOrderId: 'WO-E2E-1',
      status: 'PENDING_APPROVAL',
      validUntil: '2026-12-20',
      totalAmount: 250,
      notes: 'Replace worn brake pads.',
      items: [
        {
          id: 'QL-E2E-1',
          description: 'Brake pads',
          quantity: 1,
          unitPrice: 250,
          lineTotal: 250,
        },
      ],
    }

    await mockAuthenticatedApi(page, customerSession, async (route, url) => {
      if (url.pathname === '/api/v1/quotations/my') {
        await fulfillJson(route, [quotation])
        return true
      }
      if (url.pathname === '/api/v1/quotations/QT-E2E-1/decision') {
        quotation = {
          ...quotation,
          status:
            route.request().postDataJSON().decision === 'APPROVE'
              ? 'APPROVED'
              : 'REJECTED',
        }
        await fulfillJson(route, quotation)
        return true
      }
      return false
    })

    await page.goto('/customer/quotations')
    await page.getByLabel('Optional response note').fill('Please proceed.')
    await page.getByRole('button', { name: /Approve/ }).click()

    await expect(
      page.getByText(
        'Quotation approved. The workshop can now begin the agreed work.',
      ),
    ).toBeVisible()
    expect(quotation.status).toBe('APPROVED')
  })

  test('customer is handed off to hosted checkout for an unpaid invoice', async ({
    page,
  }) => {
    await page.route('https://checkout.stripe.test/**', (route) =>
      route.fulfill({
        contentType: 'text/html',
        body: '<title>Hosted checkout</title>',
      }),
    )
    await mockAuthenticatedApi(page, customerSession, async (route, url) => {
      if (url.pathname === '/api/v1/payments/my') {
        await fulfillJson(route, [
          {
            id: 'PAY-E2E-1',
            appointmentId: 'APT-E2E-2',
            amount: 250,
            paymentStatus: 'UNPAID',
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/payments/PAY-E2E-1/checkout') {
        await fulfillJson(route, {
          checkoutUrl: 'https://checkout.stripe.test/session/e2e',
        })
        return true
      }
      return false
    })

    await page.goto('/customer/payments')
    await Promise.all([
      page.waitForURL('https://checkout.stripe.test/session/e2e'),
      page.getByRole('button', { name: 'Pay securely' }).click(),
    ])
    await expect(page).toHaveTitle('Hosted checkout')
  })

  test('customer can see after-service progress and shared evidence', async ({
    page,
  }) => {
    await mockAuthenticatedApi(page, customerSession, async (route, url) => {
      if (
        url.pathname === '/api/v1/vehicles/my' ||
        url.pathname === '/api/v1/catalog/services' ||
        url.pathname === '/api/v1/appointments/my'
      ) {
        await fulfillJson(route, [])
        return true
      }
      if (url.pathname === '/api/v1/work-orders/my') {
        await fulfillJson(route, [
          {
            id: 'WO-E2E-3',
            vehicleId: 'VEH-E2E-1',
            serviceId: 'SVC-E2E-1',
            status: 'IN_PROGRESS',
          },
        ])
        return true
      }
      if (url.pathname === '/api/v1/work-orders/WO-E2E-3/documents') {
        await fulfillJson(route, [
          {
            id: 'DOC-E2E-1',
            fileName: 'brake-inspection.jpg',
            type: 'REPAIR_EVIDENCE',
          },
        ])
        return true
      }
      return false
    })

    await page.goto('/customer/appointments')
    await expect(page.getByText('Service work is currently underway.')).toBeVisible()
    await expect(page.getByText('brake-inspection.jpg')).toBeVisible()
    await expect(page.getByRole('link', { name: 'Download' })).toHaveAttribute(
      'href',
      '/api/v1/documents/DOC-E2E-1/download',
    )
  })

  test('a customer cannot open a staff-only work-order queue', async ({
    page,
  }) => {
    await mockAuthenticatedApi(page, customerSession, async () => false)

    await page.goto('/staff/work-orders')
    await expect(
      page.getByRole('heading', { name: '403 - Access Denied' }),
    ).toBeVisible()
  })
})
