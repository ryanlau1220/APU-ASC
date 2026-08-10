import { API_BASE_URL } from './config'
import { File, Paths } from 'expo-file-system'
import * as Sharing from 'expo-sharing'

let accessToken: string | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export async function openProtectedDocument(documentId: string, fileName: string) {
  if (!accessToken) throw new ApiError('Sign in again to open this document.', 401)

  const safeId = documentId.replace(/[^a-zA-Z0-9_-]/g, '_')
  if (!safeId) throw new ApiError('This document cannot be opened.', 400)
  const safeName = fileName.replace(/[^a-zA-Z0-9._-]/g, '_').slice(-100) || 'document'
  const url = new URL(`/api/v1/documents/${encodeURIComponent(documentId)}/download`, `${API_BASE_URL}/`)
  const destination = new File(Paths.cache, `${safeId}-${safeName}`)
  const file = await File.downloadFileAsync(url.toString(), destination, {
    headers: { Authorization: `Bearer ${accessToken}` },
    idempotent: true,
  })

  if (!(await Sharing.isAvailableAsync())) {
    throw new Error('Document sharing is unavailable on this device.')
  }
  await Sharing.shareAsync(file.uri)
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message)
  }
}

export const customInstance = async <T>(
  config: {
    url: string
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
    data?: unknown
    params?: Record<string, unknown>
    headers?: Record<string, string>
    signal?: AbortSignal
    responseType?: string
  },
  options?: RequestInit,
): Promise<T> => {
  const url = new URL(config.url, `${API_BASE_URL}/`)
  for (const [key, value] of Object.entries(config.params ?? {})) {
    if (value !== undefined && value !== null) url.searchParams.set(key, String(value))
  }

  const isFormData = config.data instanceof FormData
  const headers = new Headers(options?.headers)
  headers.set('Accept', 'application/json')
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  if (config.data !== undefined && !isFormData) {
    headers.set('Content-Type', 'application/json')
  }
  for (const [key, value] of Object.entries(config.headers ?? {})) headers.set(key, value)

  const response = await fetch(url, {
    ...options,
    method: config.method,
    headers,
    body:
      config.data === undefined
        ? undefined
        : isFormData
          ? (config.data as FormData)
          : JSON.stringify(config.data),
    signal: config.signal,
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(
      problem?.detail || problem?.message || 'The request could not be completed.',
      response.status,
    )
  }
  if (response.status === 204) return {} as T
  return response.json() as Promise<T>
}
