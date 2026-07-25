import { useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8081'

export function useEventStream() {
  const queryClient = useQueryClient()

  useEffect(() => {
    if (typeof window === 'undefined') return

    const sseUrl = `${BACKEND_URL}/api/v1/events/stream`
    const eventSource = new EventSource(sseUrl, { withCredentials: true })

    eventSource.addEventListener('invalidate', (e: MessageEvent) => {
      try {
        const payload = JSON.parse(e.data)
        if (payload?.entity) {
          const entity = payload.entity
          // Automatically invalidate TanStack Query cache across all pages
          queryClient.invalidateQueries({ queryKey: [entity] })
          queryClient.invalidateQueries({ queryKey: ['dashboard'] })
        }
      } catch {
        // Ignore JSON parse errors
      }
    })

    return () => {
      eventSource.close()
    }
  }, [queryClient])
}
