function getCsrfToken(): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

export async function bffFetch<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const method = (options.method || 'GET').toUpperCase()
  const isMutating = ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }

  if (isMutating) {
    const csrfToken = getCsrfToken()
    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken
    }
  }

  const res = await fetch(endpoint, {
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
        window.location.href = '/oauth2/authorization/keycloak'
      }
    }

    let userFriendlyDetail =
      'Invalid username or password. Please check your credentials and try again.'
    try {
      const problem = await res.json()
      userFriendlyDetail =
        problem.detail || problem.message || userFriendlyDetail
    } catch {
      // Fallback message
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
