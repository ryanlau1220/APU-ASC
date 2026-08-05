import type { QueryClient, QueryKey } from '@tanstack/react-query'
import { useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'

const TOPIC_QUERY_PREFIXES: Record<string, readonly string[]> = {
  appointments: ['/api/v1/appointments'],
  scheduling: ['/api/v1/scheduling'],
  quotations: ['/api/v1/quotations'],
  'work-orders': ['/api/v1/work-orders'],
  notifications: ['/api/v1/notifications'],
  documents: ['/api/v1/work-orders'],
}

export type LiveUpdateMessage = {
  topic: keyof typeof TOPIC_QUERY_PREFIXES
  resourceId: string | null
  occurredAt: string
}

export function invalidateLiveUpdateTopic(
  queryClient: QueryClient,
  topic: string,
) {
  const prefixes = TOPIC_QUERY_PREFIXES[topic]
  if (!prefixes) return

  queryClient.invalidateQueries({
    predicate: (query) => queryMatchesPrefixes(query.queryKey, prefixes),
    refetchType: 'active',
  })
  queryClient.invalidateQueries({
    queryKey: ['dashboard'],
    refetchType: 'active',
  })
}

export function invalidateAllLiveUpdateTopics(queryClient: QueryClient) {
  const allPrefixes = Object.values(TOPIC_QUERY_PREFIXES).flat()
  queryClient.invalidateQueries({
    predicate: (query) => queryMatchesPrefixes(query.queryKey, allPrefixes),
    refetchType: 'active',
  })
  queryClient.invalidateQueries({
    queryKey: ['dashboard'],
    refetchType: 'active',
  })
}

function queryMatchesPrefixes(queryKey: QueryKey, prefixes: readonly string[]) {
  const path = queryKey[0]
  return (
    typeof path === 'string' &&
    prefixes.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))
  )
}

export function useEventStream(enabled: boolean = true) {
  const queryClient = useQueryClient()

  useEffect(() => {
    if (typeof window === 'undefined' || !enabled) return

    const eventSource = new EventSource('/api/v1/events/stream', {
      withCredentials: true,
    })
    let hasOpened = false

    eventSource.onopen = () => {
      if (hasOpened) {
        // SSE does not replay missed messages. Refresh active live data after a reconnect.
        invalidateAllLiveUpdateTopics(queryClient)
      }
      hasOpened = true
    }

    eventSource.addEventListener(
      'live-update',
      (event: MessageEvent<string>) => {
        try {
          const update = JSON.parse(event.data) as LiveUpdateMessage
          if (update?.topic) {
            invalidateLiveUpdateTopic(queryClient, update.topic)
          }
        } catch {
          // Ignore malformed events and keep the connection alive.
        }
      },
    )

    return () => {
      eventSource.close()
    }
  }, [enabled, queryClient])
}
