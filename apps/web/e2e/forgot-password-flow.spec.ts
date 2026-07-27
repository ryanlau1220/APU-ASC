import { expect, test } from '@playwright/test'

test.describe('Forgot Password OTP User Flow', () => {
  test('should render forgot password page and interactive email input form', async ({
    page,
  }) => {
    await page.goto('/forgot-password')

    // Verify Title & Input Elements
    await expect(
      page.getByRole('heading', { name: 'Forgot Password OTP' }),
    ).toBeVisible()
    await expect(page.getByLabel('Email Address')).toBeVisible()
    await expect(
      page.getByRole('button', { name: 'Send OTP Verification Code' }),
    ).toBeVisible()
    await expect(page.getByRole('link', { name: 'Back to Sign In' })).toBeVisible()

    // Input Email and Submit Form
    await page.getByLabel('Email Address').fill('customer@apu-asc.com')
    await page
      .getByRole('button', { name: 'Send OTP Verification Code' })
      .click()
  })
})
