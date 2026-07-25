import { describe, expect, it, vi } from 'vitest'
import { bffFetch } from './lib/apiClient'

describe('bffFetch API Client', () => {
  it('should successfully parse JSON response for 200 OK', async () => {
    const mockData = { id: 'USR-101', name: 'Alex Tan' }
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockData,
    })

    const data = await bffFetch<{ id: string; name: string }>(
      '/api/v1/users/USR-101',
    )
    expect(data).toEqual(mockData)
  })

  it('should throw formatted RFC 9457 error message on HTTP error', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      json: async () => ({
        type: 'https://apu-asc.com/errors/validation-error',
        title: 'Validation Failure',
        status: 400,
        detail: 'Validation failed for one or more request parameters',
      }),
    })

    await expect(
      bffFetch('/api/v1/auth/login', { method: 'POST' }),
    ).rejects.toThrow(
      '[400] Validation failed for one or more request parameters',
    )
  })
})
