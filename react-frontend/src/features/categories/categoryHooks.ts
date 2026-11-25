import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { categoryApi } from './categoryApi'
import type { CategoryRequest } from './categoryTypes'

export const categoryKeys = {
  all: ['categories'] as const,
}

export function useCategories() {
  return useQuery({
    queryKey: categoryKeys.all,
    queryFn: ({ signal }) => categoryApi.list(signal),
  })
}

export function useSaveCategory(id?: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CategoryRequest) =>
      id === undefined ? categoryApi.create(request) : categoryApi.update(id, request),
    // Products show their category name, so refresh products as well.
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => categoryApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: categoryKeys.all }),
  })
}
