import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { PageParams } from '@/api/types'

import { orderApi } from './orderApi'
import type { OrderFilters, OrderRequest, OrderStatus } from './types'

export const orderKeys = {
  all: ['orders'] as const,
  list: (params: PageParams, filters: OrderFilters) => [...orderKeys.all, 'list', params, filters] as const,
  detail: (id: number) => [...orderKeys.all, 'detail', id] as const,
}

export function useOrders(params: PageParams, filters: OrderFilters = {}) {
  return useQuery({
    queryKey: orderKeys.list(params, filters),
    queryFn: ({ signal }) => orderApi.search(params, filters, signal),
    placeholderData: keepPreviousData,
  })
}

export function useOrder(id: number) {
  return useQuery({
    queryKey: orderKeys.detail(id),
    queryFn: ({ signal }) => orderApi.get(id, signal),
  })
}

export function useCreateOrder() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: OrderRequest) => orderApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}

export function useUpdateOrderStatus(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (status: OrderStatus) => orderApi.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}
