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
