import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { PageParams } from '@/api/types'

import { productApi } from './productApi'
import type { ProductFilters, ProductRequest } from './types'

export const productKeys = {
  all: ['products'] as const,
  list: (params: PageParams, filters: ProductFilters) => [...productKeys.all, 'list', params, filters] as const,
  detail: (id: number) => [...productKeys.all, 'detail', id] as const,
}

export function useProducts(params: PageParams, filters: ProductFilters = {}) {
  return useQuery({
    queryKey: productKeys.list(params, filters),
    queryFn: ({ signal }) => productApi.search(params, filters, signal),
    placeholderData: keepPreviousData,
  })
}

export function useProduct(id: number | undefined) {
  return useQuery({
    queryKey: productKeys.detail(id ?? 0),
    queryFn: ({ signal }) => productApi.get(id as number, signal),
    enabled: id !== undefined,
  })
}

export function useSaveProduct(id?: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ProductRequest) =>
      id === undefined ? productApi.create(request) : productApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: productKeys.all }),
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => productApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: productKeys.all }),
  })
}
