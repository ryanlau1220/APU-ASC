import { QueryClient } from '@tanstack/react-query'
import { describe, expect, it } from 'vitest'
import {
  invalidateAllLiveUpdateTopics,
  invalidateLiveUpdateTopic,
} from './useEventStream'

function queryClientWithLiveData() {
  const queryClient = new QueryClient()
  queryClient.setQueryData(['/api/v1/appointments/my'], ['appointment'])
  queryClient.setQueryData(['/api/v1/work-orders/my'], ['work-order'])
  queryClient.setQueryData(['/api/v1/quotations/my'], ['quotation'])
  queryClient.setQueryData(
    ['/api/v1/scheduling/availability', { date: '2026-08-04' }],
    ['slot'],
  )
  queryClient.setQueryData(['/api/v1/vehicles/my'], ['vehicle'])
  return queryClient
}

describe('live-update cache invalidation', () => {
  it('invalidates every matching generated Orval query key for a topic', () => {
    const queryClient = queryClientWithLiveData()

    invalidateLiveUpdateTopic(queryClient, 'appointments')

    expect(
      queryClient.getQueryState(['/api/v1/appointments/my'])?.isInvalidated,
    ).toBe(true)
    expect(
      queryClient.getQueryState(['/api/v1/work-orders/my'])?.isInvalidated,
    ).toBe(false)
  })

  it('invalidates all live-data topics after an SSE reconnect', () => {
    const queryClient = queryClientWithLiveData()

    invalidateAllLiveUpdateTopics(queryClient)

    expect(
      queryClient.getQueryState(['/api/v1/appointments/my'])?.isInvalidated,
    ).toBe(true)
    expect(
      queryClient.getQueryState(['/api/v1/work-orders/my'])?.isInvalidated,
    ).toBe(true)
    expect(
      queryClient.getQueryState(['/api/v1/quotations/my'])?.isInvalidated,
    ).toBe(true)
    expect(
      queryClient.getQueryState([
        '/api/v1/scheduling/availability',
        { date: '2026-08-04' },
      ])?.isInvalidated,
    ).toBe(true)
    expect(
      queryClient.getQueryState(['/api/v1/vehicles/my'])?.isInvalidated,
    ).toBe(false)
  })
})
