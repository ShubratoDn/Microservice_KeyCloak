import { humanize } from '@/lib/format'

type Tone = 'success' | 'info' | 'warning' | 'danger'

const TONES: Record<string, Tone> = {
  ACTIVE: 'success',
  DELIVERED: 'success',
  PAID: 'success',
  CONFIRMED: 'info',
  SHIPPED: 'info',
  PENDING: 'warning',
  DRAFT: 'warning',
  INACTIVE: 'warning',
  OUT_OF_STOCK: 'warning',
  ADMIN: 'info',
  MANAGER: 'success',
  STAFF: 'warning',
  BLOCKED: 'danger',
  CANCELLED: 'danger',
  DISCONTINUED: 'danger',
}

export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-${TONES[status] ?? 'info'}`}>{humanize(status)}</span>
}
