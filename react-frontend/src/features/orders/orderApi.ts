import { http } from '@/api/httpClient'
import type { PageParams, PagedResponse } from '@/api/types'

import type { Order, OrderFilters, OrderRequest, OrderStatus, OrderSummary } from './types'

const BASE = '/api/v1/orders'

export const orderApi = {
  search: (params: PageParams, filters: OrderFilters, signal?: AbortSignal) =>
    http.get<PagedResponse<OrderSummary>>(BASE, { query: { ...params, ...filters }, signal }),
  get: (id: number, signal?: AbortSignal) => http.get<Order>(`${BASE}/${id}`, { signal }),
  create: (request: OrderRequest) => http.post<Order>(BASE, request),
  updateStatus: (id: number, status: OrderStatus) => http.patch<Order>(`${BASE}/${id}/status`, { status }),
}
