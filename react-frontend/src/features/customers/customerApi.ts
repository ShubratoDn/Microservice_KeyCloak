import { http } from '@/api/httpClient'
import type { PageParams, PagedResponse } from '@/api/types'

import type { Customer, CustomerRequest, CustomerStatus, CustomerSummary } from './types'

const BASE = '/api/v1/customers'

export const customerApi = {
  list: (params: PageParams, signal?: AbortSignal) =>
    http.get<PagedResponse<CustomerSummary>>(BASE, { query: { ...params }, signal }),
  get: (id: number, signal?: AbortSignal) => http.get<Customer>(`${BASE}/${id}`, { signal }),
  create: (request: CustomerRequest) => http.post<Customer>(BASE, request),
  update: (id: number, request: CustomerRequest) => http.put<Customer>(`${BASE}/${id}`, request),
  updateStatus: (id: number, status: CustomerStatus) => http.patch<Customer>(`${BASE}/${id}/status`, { status }),
  remove: (id: number) => http.delete(`${BASE}/${id}`),
}
