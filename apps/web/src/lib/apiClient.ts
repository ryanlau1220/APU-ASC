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
      if (!isAuthPage) {
        localStorage.removeItem('apu_asc_token')
        window.location.href = '/login'
      }
    }

    let userFriendlyDetail =
      'Invalid username or password. Please check your credentials and try again.'
    try {
      const problem = await res.json()
      userFriendlyDetail =
        problem.detail || problem.message || userFriendlyDetail
    } catch {
      // Fallback to human friendly message
    }
    throw new Error(userFriendlyDetail)
  }

  if (!res.ok) {
    let errorDetail = 'An unexpected error occurred. Please try again.'
    try {
      const problem = await res.json()
      errorDetail = problem.detail || problem.message || errorDetail
    } catch {
      if (res.status === 403) {
        errorDetail = 'You do not have permission to perform this action.'
      } else if (res.status === 404) {
        errorDetail = 'The requested resource could not be found.'
      } else if (res.status >= 500) {
        errorDetail = 'A server error occurred. Please try again later.'
      }
    }
    throw new Error(errorDetail)
  }

  if (res.status === 204) {
    return {} as T
  }

  return res.json()
}
