
import { nextStatuses } from './orderStatus'

describe('nextStatuses', () => {
  it('follows the same flow as the order service', () => {
    expect(nextStatuses('PENDING')).toEqual(['CONFIRMED', 'CANCELLED'])
    expect(nextStatuses('CONFIRMED')).toEqual(['PAID', 'CANCELLED'])
    expect(nextStatuses('PAID')).toEqual(['SHIPPED', 'CANCELLED'])
    expect(nextStatuses('SHIPPED')).toEqual(['DELIVERED'])
  })

  it('has no next step for finished orders', () => {
    expect(nextStatuses('DELIVERED')).toEqual([])
    expect(nextStatuses('CANCELLED')).toEqual([])
  })
})
