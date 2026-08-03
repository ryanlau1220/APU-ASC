import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { bffFetch } from '../lib/apiClient'

export interface WorkOrderDto {
  id?: string
  appointmentId?: string
  customerId?: string
  vehicleId?: string
  serviceId?: string
  technicianId?: string
  status?: string
  intakeNotes?: string
  diagnosticNotes?: string
  openedAt?: string
  startedAt?: string
  completedAt?: string
  createdAt?: string
  updatedAt?: string
}

const workOrderKey = ['work-orders'] as const

function invalidateWorkOrders(queryClient: ReturnType<typeof useQueryClient>) {
  return queryClient.invalidateQueries({ queryKey: workOrderKey })
}

export function useGetAllWorkOrders() {
  return useQuery({
    queryKey: [...workOrderKey, 'all'],
    queryFn: () => bffFetch<WorkOrderDto[]>('/api/v1/work-orders'),
  })
}

export function useGetMyWorkOrders() {
  return useQuery({
    queryKey: [...workOrderKey, 'my'],
    queryFn: () => bffFetch<WorkOrderDto[]>('/api/v1/work-orders/my'),
  })
}

export function useCreateWorkOrder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: WorkOrderDto) =>
      bffFetch<WorkOrderDto>('/api/v1/work-orders', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    onSuccess: () => invalidateWorkOrders(queryClient),
  })
}

export function useUpdateWorkOrder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: WorkOrderDto }) =>
      bffFetch<WorkOrderDto>(`/api/v1/work-orders/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    onSuccess: () => invalidateWorkOrders(queryClient),
  })
}

export function useUpdateWorkOrderStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      bffFetch<WorkOrderDto>(
        `/api/v1/work-orders/${id}/status?status=${encodeURIComponent(status)}`,
        { method: 'PATCH' },
      ),
    onSuccess: () => invalidateWorkOrders(queryClient),
  })
}

export function useUpdateDiagnosticNotes() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes: string }) =>
      bffFetch<WorkOrderDto>(`/api/v1/work-orders/${id}/diagnostic-notes`, {
        method: 'PATCH',
        body: JSON.stringify(notes),
      }),
    onSuccess: () => invalidateWorkOrders(queryClient),
  })
}
