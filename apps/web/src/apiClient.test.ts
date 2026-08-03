import { describe, expect, it, vi } from 'vitest'
import { bffFetch, customInstance } from './lib/apiClient'

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
    ).rejects.toThrow('Validation failed for one or more request parameters')
  })

  it('should throw user-friendly error when session expired or 401', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({
        message: 'Invalid username or password',
      }),
    })

    await expect(bffFetch('/api/v1/vehicles')).rejects.toThrow(
      'Invalid username or password',
    )
  })

  it('should preserve FormData and let the browser set its multipart boundary', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ id: 'USR-101' }),
    })
    const formData = new FormData()
    formData.append('file', new Blob(['avatar']), 'avatar.png')

    await customInstance({
      url: '/api/v1/users/avatar',
      method: 'POST',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    const [, options] = vi.mocked(global.fetch).mock.calls[0]
    expect(options?.body).toBe(formData)
    expect(
      (options?.headers as Record<string, string>)['Content-Type'],
    ).toBeUndefined()
  })
})
