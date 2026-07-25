const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8081'

export async function bffFetch<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const url = `${BACKEND_URL}${endpoint}`

  const token =
    typeof window !== 'undefined' ? localStorage.getItem('apu_asc_token') : null

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers as Record<string, string>),
  }

  // Support both HttpOnly cookies and Bearer token headers
  const res = await fetch(url, {
    credentials: 'include',
    ...options,
    headers,
  })

  if (res.status === 401) {
    if (typeof window !== 'undefined') {
      const isAuthPage =
        window.location.pathname.startsWith('/login') ||
        window.location.pathname.startsWith('/register')
      if (!isAuthPage && token) {
        localStorage.removeItem('apu_asc_token')
        window.location.href = '/login'
      }
    }
    throw new Error('[401] Session expired or unauthorized')
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
