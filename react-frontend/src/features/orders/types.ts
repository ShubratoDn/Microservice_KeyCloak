export const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'] as const
export type OrderStatus = (typeof ORDER_STATUSES)[number]

export interface OrderSummary {
  id: number
  orderNumber: string
  customerId: number
  status: OrderStatus
  totalAmount: number
  createdAt: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  sku: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order extends OrderSummary {
  shippingAddress: string
  notes: string | null
  items: OrderItem[]
  updatedAt: string
}

export interface OrderRequest {
  customerId: number
  shippingAddress: string
  notes?: string
  items: { productId: number; quantity: number }[]
}

export interface OrderFilters {
  status?: OrderStatus
  customerId?: number
}
