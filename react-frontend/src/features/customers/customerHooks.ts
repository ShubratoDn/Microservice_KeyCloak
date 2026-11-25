import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { PageParams } from '@/api/types'

import { customerApi } from './customerApi'
import type { CustomerRequest, CustomerStatus } from './types'

export const customerKeys = {
  all: ['customers'] as const,
  list: (params: PageParams) => [...customerKeys.all, 'list', params] as const,
  detail: (id: number) => [...customerKeys.all, 'detail', id] as const,
}

export function useCustomers(params: PageParams) {
  return useQuery({
    queryKey: customerKeys.list(params),
    queryFn: ({ signal }) => customerApi.list(params, signal),
    placeholderData: keepPreviousData,
  })
}

export function useCustomer(id: number | undefined) {
  return useQuery({
    queryKey: customerKeys.detail(id ?? 0),
    queryFn: ({ signal }) => customerApi.get(id as number, signal),
    enabled: id !== undefined,
  })
}

export function useSaveCustomer(id?: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CustomerRequest) =>
      id === undefined ? customerApi.create(request) : customerApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: customerKeys.all }),
  })
}

export function useUpdateCustomerStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: CustomerStatus }) => customerApi.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: customerKeys.all }),
  })
}

export function useDeleteCustomer() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => customerApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: customerKeys.all }),
  })
}
