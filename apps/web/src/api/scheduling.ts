import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { bffFetch } from '../lib/apiClient'

export interface SlotAvailability {
  appointmentDate: string
  timeSlot: string
  capacity: number
  reserved: number
  available: number
  bookable: boolean
}

export interface SlotCapacityUpdate {
  appointmentDate: string
  timeSlot: string
  capacity: number
}

const schedulingKey = ['scheduling'] as const

export function useSlotAvailability(appointmentDate: string) {
  return useQuery({
    queryKey: [...schedulingKey, 'availability', appointmentDate],
    enabled: Boolean(appointmentDate),
    queryFn: () =>
      bffFetch<SlotAvailability[]>(
        `/api/v1/scheduling/availability?date=${encodeURIComponent(appointmentDate)}`,
      ),
  })
}

export function useUpdateSlotCapacity() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: SlotCapacityUpdate) =>
      bffFetch<SlotAvailability>('/api/v1/scheduling/capacity', {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    onSuccess: (_, variables) =>
      queryClient.invalidateQueries({
        queryKey: [...schedulingKey, 'availability', variables.appointmentDate],
      }),
  })
}
