import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'

import { getErrorMessage } from '@/api/ApiError'
import { Button } from '@/components/Button'
import { ErrorState } from '@/components/ErrorState'
import { FormField } from '@/components/FormField'
import { PageHeader } from '@/components/PageHeader'
import { Spinner } from '@/components/Spinner'
import { useToast } from '@/components/toast/toastContext'
import { useCategories } from '@/features/categories/categoryHooks'
import { applyServerErrors, blankToUndefined } from '@/lib/forms'
import { humanize } from '@/lib/format'

import { useProduct, useSaveProduct } from './productHooks'
import { PRODUCT_STATUSES, type Product, type ProductRequest, type ProductStatus } from './types'

interface ProductFormValues {
  name: string
  description: string
  sku: string
  price: string
  stockQuantity: string
  status: ProductStatus
  categoryId: string
}

const EMPTY_FORM: ProductFormValues = {
  name: '',
  description: '',
  sku: '',
  price: '',
  stockQuantity: '0',
  status: 'DRAFT',
  categoryId: '',
}

function toFormValues(product: Product): ProductFormValues {
  return {
    name: product.name,
    description: product.description ?? '',
    sku: product.sku,
    price: String(product.price),
    stockQuantity: String(product.stockQuantity),
    status: product.status,
    categoryId: String(product.category.id),
  }
}

function toRequest(values: ProductFormValues): ProductRequest {
  return {
    name: values.name.trim(),
    description: blankToUndefined(values.description),
    sku: values.sku.trim(),
    price: Number(values.price),
    stockQuantity: Number(values.stockQuantity),
    status: values.status,
    categoryId: Number(values.categoryId),
  }
}

export function ProductFormPage() {
  const params = useParams()
  const productId = params.id ? Number(params.id) : undefined
  const isEdit = productId !== undefined
  const navigate = useNavigate()
  const toast = useToast()

  const product = useProduct(productId)
  const categories = useCategories()
  const save = useSaveProduct(productId)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<ProductFormValues>({ values: product.data ? toFormValues(product.data) : EMPTY_FORM })

  function onSubmit(values: ProductFormValues) {
    save.mutate(toRequest(values), {
      onSuccess: () => {
        toast.success(isEdit ? 'Product updated' : 'Product created')
        navigate('/products')
      },
      onError: (error) => {
        if (!applyServerErrors(error, setError)) {
          toast.error(getErrorMessage(error))
        }
      },
    })
  }

  if ((isEdit && product.isPending) || categories.isPending) {
    return <Spinner />
  }
  if (isEdit && product.isError) {
    return <ErrorState error={product.error} onRetry={() => void product.refetch()} />
  }
  if (categories.isError) {
    return <ErrorState error={categories.error} onRetry={() => void categories.refetch()} />
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Edit product' : 'New product'} />
      <form className="card form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="form-grid">
          <FormField label="Name" htmlFor="name" error={errors.name?.message}>
            <input id="name" maxLength={200} {...register('name', { required: 'Product name is required' })} />
          </FormField>
          <FormField
            label="SKU"
            htmlFor="sku"
            hint="Letters, numbers, hyphen and underscore."
            error={errors.sku?.message}
          >
            <input
              id="sku"
              maxLength={50}
              {...register('sku', {
                required: 'SKU is required',
                pattern: {
                  value: /^[A-Za-z0-9][A-Za-z0-9_-]*$/,
                  message: 'SKU may contain only letters, numbers, hyphen and underscore',
                },
              })}
            />
          </FormField>
          <FormField label="Category" htmlFor="categoryId" error={errors.categoryId?.message}>
            <select id="categoryId" {...register('categoryId', { required: 'Category is required' })}>
              <option value="">Select a category…</option>
              {categories.data?.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Status" htmlFor="status" error={errors.status?.message}>
            <select id="status" {...register('status')}>
              {PRODUCT_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {humanize(status)}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Price" htmlFor="price" error={errors.price?.message}>
            <input
              id="price"
              type="number"
              step="0.01"
              min="0"
              {...register('price', {
                required: 'Price is required',
                min: { value: 0, message: 'Price cannot be negative' },
              })}
            />
          </FormField>
          <FormField label="Stock quantity" htmlFor="stockQuantity" error={errors.stockQuantity?.message}>
            <input
              id="stockQuantity"
              type="number"
              step="1"
              min="0"
              {...register('stockQuantity', { min: { value: 0, message: 'Stock quantity cannot be negative' } })}
            />
          </FormField>
        </div>
        <FormField label="Description" htmlFor="description" error={errors.description?.message}>
          <textarea
            id="description"
            rows={4}
            {...register('description', {
              maxLength: { value: 2000, message: 'Description must be at most 2000 characters' },
            })}
          />
        </FormField>

        <div className="row row-end">
          <Button onClick={() => navigate('/products')}>Cancel</Button>
          <Button type="submit" variant="primary" loading={save.isPending}>
            {isEdit ? 'Save changes' : 'Create product'}
          </Button>
        </div>
      </form>
    </>
  )
}
