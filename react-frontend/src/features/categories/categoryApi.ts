import { http } from '@/api/httpClient'

import type { Category, CategoryRequest } from './categoryTypes'

const BASE = '/api/v1/categories'

export const categoryApi = {
  list: (signal?: AbortSignal) => http.get<Category[]>(BASE, { signal }),
  create: (request: CategoryRequest) => http.post<Category>(BASE, request),
  update: (id: number, request: CategoryRequest) => http.put<Category>(`${BASE}/${id}`, request),
  remove: (id: number) => http.delete(`${BASE}/${id}`),
}
