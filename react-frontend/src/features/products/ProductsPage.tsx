import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router-dom'

import { getErrorMessage } from '@/api/ApiError'
import { Permissions } from '@/auth/permissions'
import { useCurrentUser } from '@/auth/useCurrentUser'
import { Button } from '@/components/Button'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { PageHeader } from '@/components/PageHeader'
import { Pagination } from '@/components/Pagination'
import { Spinner } from '@/components/Spinner'
import { StatusBadge } from '@/components/StatusBadge'
import { useToast } from '@/components/toast/toastContext'
import { useCategories } from '@/features/categories/categoryHooks'
import { formatMoney, humanize } from '@/lib/format'

import { useDeleteProduct, useProducts } from './productHooks'
import { PRODUCT_STATUSES, type Product, type ProductStatus } from './types'

const PAGE_SIZE = 10

export function ProductsPage() {
  const { can } = useCurrentUser()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<ProductStatus | ''>('')
  const [categoryId, setCategoryId] = useState('')
  const [toDelete, setToDelete] = useState<Product | null>(null)

  // Wait until the user stops typing before asking the server.
  const deferredSearch = useDeferredValue(search.trim())

  const products = useProducts(
    { page, size: PAGE_SIZE },
    {
      q: deferredSearch || undefined,
      status: status || undefined,
      categoryId: categoryId ? Number(categoryId) : undefined,
    },
  )
  const categories = useCategories()
  const deleteProduct = useDeleteProduct()

  function confirmDelete() {
    if (!toDelete) {
      return
    }
    deleteProduct.mutate(toDelete.id, {
      onSuccess: () => toast.success('Product deleted'),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setToDelete(null),
    })
  }

  return (
    <>
      <PageHeader
        title="Products"
        subtitle="What you sell"
        actions={
          can(Permissions.PRODUCT_ADD) && (
            <Link className="btn btn-primary" to="/products/new">
              New product
            </Link>
          )
        }
      />

      <div className="card filters">
        <input
          type="search"
          placeholder="Search by name or SKU…"
          aria-label="Search products"
          value={search}
          onChange={(event) => {
            setSearch(event.target.value)
            setPage(0)
          }}
        />
        <select
          aria-label="Filter by status"
          value={status}
          onChange={(event) => {
            setStatus(event.target.value as ProductStatus | '')
            setPage(0)
          }}
        >
          <option value="">All statuses</option>
          {PRODUCT_STATUSES.map((value) => (
            <option key={value} value={value}>
              {humanize(value)}
            </option>
          ))}
        </select>
        <select
          aria-label="Filter by category"
          value={categoryId}
          onChange={(event) => {
            setCategoryId(event.target.value)
            setPage(0)
          }}
        >
          <option value="">All categories</option>
          {categories.data?.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>

      {products.isPending && <Spinner />}
      {products.isError && <ErrorState error={products.error} onRetry={() => void products.refetch()} />}

      {products.data && (
        <div className="card">
          {products.data.content.length === 0 ? (
            <EmptyState message="No products found." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>SKU</th>
                    <th>Category</th>
                    <th className="num">Price</th>
                    <th className="num">Stock</th>
                    <th>Status</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {products.data.content.map((product) => (
                    <tr key={product.id}>
                      <td>{product.name}</td>
                      <td>
                        <code>{product.sku}</code>
                      </td>
                      <td>{product.category.name}</td>
                      <td className="num">{formatMoney(product.price)}</td>
                      <td className="num">{product.stockQuantity}</td>
                      <td>
                        <StatusBadge status={product.status} />
                      </td>
                      <td className="row row-end">
                        {can(Permissions.PRODUCT_EDIT) && (
                          <Link className="btn btn-secondary" to={`/products/${product.id}/edit`}>
                            Edit
                          </Link>
                        )}
                        {can(Permissions.PRODUCT_DELETE) && (
                          <Button variant="danger" onClick={() => setToDelete(product)}>
                            Delete
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <Pagination
            page={products.data.page}
            totalPages={products.data.totalPages}
            totalElements={products.data.totalElements}
            onPageChange={setPage}
          />
        </div>
      )}

      <ConfirmDialog
        open={toDelete !== null}
        danger
        title="Delete product"
        message={`Delete “${toDelete?.name ?? ''}”? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleteProduct.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  )
}
