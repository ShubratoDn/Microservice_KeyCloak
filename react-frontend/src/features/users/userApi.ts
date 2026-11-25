import { http } from '@/api/httpClient'
import type { PageParams, PagedResponse } from '@/api/types'

import type { CreateUserRequest, UpdateUserRequest, User, UserFilters, UserSummary } from './types'

const BASE = '/api/v1/users'

export const userApi = {
  search: (params: PageParams, filters: UserFilters, signal?: AbortSignal) =>
    http.get<PagedResponse<UserSummary>>(BASE, { query: { ...params, ...filters }, signal }),
  get: (id: number, signal?: AbortSignal) => http.get<User>(`${BASE}/${id}`, { signal }),
  create: (request: CreateUserRequest) => http.post<User>(BASE, request),
  update: (id: number, request: UpdateUserRequest) => http.put<User>(`${BASE}/${id}`, request),
  updateStatus: (id: number, enabled: boolean) => http.patch<User>(`${BASE}/${id}/status`, { enabled }),
  remove: (id: number) => http.delete(`${BASE}/${id}`),
}
