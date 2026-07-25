const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8081'

export async function bffFetch<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const url = `${BACKEND_URL}${endpoint}`
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }

  const res = await fetch(url, { ...options, headers })

  if (res.status === 401) {
    if (
      typeof window !== 'undefined' &&
      !window.location.pathname.startsWith('/login') &&
      !window.location.pathname.startsWith('/register')
    ) {
      window.location.href = '/login'
    }
    throw new Error('[401] Unauthorized session. Redirecting to login...')
  }

  if (!res.ok) {
    let errorDetail = 'API request failed'
    try {
      const problem = await res.json()
      errorDetail = problem.detail || problem.message || errorDetail
    } catch {
      // Ignore JSON parse error fallback
    }
    throw new Error(`[${res.status}] ${errorDetail}`)
  }

  if (res.status === 204) {
    return {} as T
  }

  return res.json()
}
