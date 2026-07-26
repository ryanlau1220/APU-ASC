import { expect, test } from '@playwright/test'

test.describe('Authentication & Dashboard User Journey', () => {
  test('should navigate to login page and initiate Keycloak SSO redirect', async ({
    page,
  }) => {
    await page.goto('/login', { waitUntil: 'commit' })
    await expect(page).toHaveURL(/.*(oauth2\/authorization\/keycloak|auth\/realms\/apu-asc).*/, { timeout: 15000 })
  })

  test('should render service catalog page', async ({ page }) => {
    await page.goto('/services')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })
})
