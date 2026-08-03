import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { bffFetch } from '../lib/apiClient'

export type QuotationStatus =
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'EXPIRED'

export interface QuotationLine {
  id?: string
  description: string
  quantity: number
  unitPrice: number
  lineTotal?: number
}

export interface QuotationDto {
  id: string
  quoteNumber: string
  workOrderId: string
  customerId: string
  revision: number
  status: QuotationStatus
  notes?: string
  responseNotes?: string
  validUntil: string
  subtotal: number
  taxAmount: number
  totalAmount: number
  submittedAt?: string
  respondedAt?: string
  createdAt?: string
  updatedAt?: string
  items: QuotationLine[]
}

export interface QuotationDraftRequest {
  workOrderId: string
  notes?: string
  validUntil: string
  items: QuotationLine[]
}

const quotationKey = ['quotations'] as const

function invalidateQuotations(queryClient: ReturnType<typeof useQueryClient>) {
  return queryClient.invalidateQueries({ queryKey: quotationKey })
}

export function useGetAllQuotations() {
  return useQuery({
    queryKey: [...quotationKey, 'all'],
    queryFn: () => bffFetch<QuotationDto[]>('/api/v1/quotations'),
  })
}

export function useGetMyQuotations() {
  return useQuery({
    queryKey: [...quotationKey, 'my'],
    queryFn: () => bffFetch<QuotationDto[]>('/api/v1/quotations/my'),
  })
}

export function useCreateQuotation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: QuotationDraftRequest) =>
      bffFetch<QuotationDto>('/api/v1/quotations', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    onSuccess: () => invalidateQuotations(queryClient),
  })
}

export function useSubmitQuotation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      bffFetch<QuotationDto>(`/api/v1/quotations/${id}/submit`, {
        method: 'POST',
      }),
    onSuccess: () => invalidateQuotations(queryClient),
  })
}

export function useDecideQuotation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      id,
      decision,
      responseNotes,
    }: {
      id: string
      decision: 'APPROVE' | 'REJECT'
      responseNotes?: string
    }) =>
      bffFetch<QuotationDto>(`/api/v1/quotations/${id}/decision`, {
        method: 'POST',
        body: JSON.stringify({ decision, responseNotes }),
      }),
    onSuccess: () => invalidateQuotations(queryClient),
  })
}
