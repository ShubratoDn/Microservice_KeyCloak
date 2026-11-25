import { http } from '@/api/httpClient'
import type { PageParams, PagedResponse } from '@/api/types'

import type { Product, ProductFilters, ProductRequest } from './types'

const BASE = '/api/v1/products'

export const productApi = {
  search: (params: PageParams, filters: ProductFilters, signal?: AbortSignal) =>
    http.get<PagedResponse<Product>>(BASE, { query: { ...params, ...filters }, signal }),
  get: (id: number, signal?: AbortSignal) => http.get<Product>(`${BASE}/${id}`, { signal }),
  create: (request: ProductRequest) => http.post<Product>(BASE, request),
  update: (id: number, request: ProductRequest) => http.put<Product>(`${BASE}/${id}`, request),
  remove: (id: number) => http.delete(`${BASE}/${id}`),
}
