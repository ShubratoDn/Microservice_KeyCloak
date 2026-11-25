import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { PageParams } from '@/api/types'

import { userApi } from './userApi'
import type { CreateUserRequest, UpdateUserRequest, UserFilters } from './types'

export const userKeys = {
  all: ['users'] as const,
  list: (params: PageParams, filters: UserFilters) => [...userKeys.all, 'list', params, filters] as const,
  detail: (id: number) => [...userKeys.all, 'detail', id] as const,
}

export function useUsers(params: PageParams, filters: UserFilters = {}) {
  return useQuery({
    queryKey: userKeys.list(params, filters),
    queryFn: ({ signal }) => userApi.search(params, filters, signal),
    placeholderData: keepPreviousData,
  })
}

export function useUser(id: number | undefined) {
  return useQuery({
    queryKey: userKeys.detail(id ?? 0),
    queryFn: ({ signal }) => userApi.get(id as number, signal),
    enabled: id !== undefined,
  })
}

export function useRegisterUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateUserRequest) => userApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}

export function useUpdateUser(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: UpdateUserRequest) => userApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}

export function useUpdateUserStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, enabled }: { id: number; enabled: boolean }) => userApi.updateStatus(id, enabled),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}

export function useDeleteUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => userApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: userKeys.all }),
  })
}
