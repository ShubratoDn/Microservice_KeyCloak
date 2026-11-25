import type { OrderStatus } from './types'

// Same rules as OrderStatus.canTransitionTo() in the order service.
// The UI uses it to show only the buttons that can work. The backend still decides.
const NEXT_STATUSES: Record<OrderStatus, readonly OrderStatus[]> = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['PAID', 'CANCELLED'],
  PAID: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

export function nextStatuses(status: OrderStatus): readonly OrderStatus[] {
  return NEXT_STATUSES[status]
}
