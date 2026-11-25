export const PRODUCT_STATUSES = ['DRAFT', 'ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED'] as const
export type ProductStatus = (typeof PRODUCT_STATUSES)[number]

export interface Product {
  id: number
  name: string
  description: string | null
  sku: string
  price: number
  stockQuantity: number
  status: ProductStatus
  category: { id: number; name: string }
  createdAt: string
  updatedAt: string
}

export interface ProductRequest {
  name: string
  description?: string
  sku: string
  price: number
  stockQuantity: number
  status: ProductStatus
  categoryId: number
}

export interface ProductFilters {
  q?: string
  status?: ProductStatus
  categoryId?: number
}
