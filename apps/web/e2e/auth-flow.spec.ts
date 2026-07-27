import { expect, test } from '@playwright/test'

test.describe('Authentication & Dashboard User Journey', () => {
  test('should render service catalog page', async ({ page }) => {
    await page.goto('/services')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })

  test('should render forgot password page', async ({ page }) => {
    await page.goto('/forgot-password')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })
})
