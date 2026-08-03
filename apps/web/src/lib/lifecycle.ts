export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED'
export type WorkOrderStatus =
  | 'OPEN'
  | 'DIAGNOSING'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export function appointmentTransitionTargets(
  status?: string,
): AppointmentStatus[] {
  switch (status) {
    case 'PENDING':
      return ['CONFIRMED', 'CANCELLED']
    case 'CONFIRMED':
      return ['CANCELLED']
    default:
      return []
  }
}

export function workOrderTransitionTargets(status?: string): WorkOrderStatus[] {
  switch (status) {
    case 'OPEN':
      return ['DIAGNOSING', 'CANCELLED']
    case 'DIAGNOSING':
      return ['IN_PROGRESS', 'CANCELLED']
    case 'IN_PROGRESS':
      return ['COMPLETED', 'CANCELLED']
    default:
      return []
  }
}

export const workOrderTransitionLabels: Record<WorkOrderStatus, string> = {
  OPEN: 'Open',
  DIAGNOSING: 'Begin diagnosis',
  IN_PROGRESS: 'Start servicing',
  COMPLETED: 'Mark completed',
  CANCELLED: 'Cancel work order',
}
