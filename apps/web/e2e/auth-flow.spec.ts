import { expect, test } from '@playwright/test'

test.describe('Authentication & Dashboard User Journey', () => {
  test('should navigate to login page and render form elements', async ({
    page,
  }) => {
    await page.goto('/login')
    await expect(
      page.getByRole('heading', { name: /Sign In to APU-ASC/i }),
    ).toBeVisible()
    await expect(page.getByLabel(/Username or Email Address/i)).toBeVisible()
    await expect(page.getByLabel(/Password/i)).toBeVisible()
  })

  test('should render service catalog page', async ({ page }) => {
    await page.goto('/services')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })
})
