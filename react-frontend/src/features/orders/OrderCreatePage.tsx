import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'

import { ApiError, getErrorMessage } from '@/api/ApiError'
import { Button } from '@/components/Button'
import { ErrorState } from '@/components/ErrorState'
import { FormField } from '@/components/FormField'
import { PageHeader } from '@/components/PageHeader'
import { Spinner } from '@/components/Spinner'
import { useToast } from '@/components/toast/toastContext'
import { useCustomers } from '@/features/customers/customerHooks'
import { useProducts } from '@/features/products/productHooks'
import { applyServerErrors, blankToUndefined } from '@/lib/forms'
import { formatMoney } from '@/lib/format'

import { useCreateOrder } from './orderHooks'

interface OrderFormValues {
  customerId: string
  shippingAddress: string
  notes: string
  items: { productId: string; quantity: string }[]
}

const EMPTY_ITEM = { productId: '', quantity: '1' }

export function OrderCreatePage() {
  const navigate = useNavigate()
  const toast = useToast()

  // Creating an order needs the lists of customers and products (their VIEW roles).
  const customers = useCustomers({ page: 0, size: 200 })
  const products = useProducts({ page: 0, size: 200 }, { status: 'ACTIVE' })
  const createOrder = useCreateOrder()

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<OrderFormValues>({
    defaultValues: { customerId: '', shippingAddress: '', notes: '', items: [EMPTY_ITEM] },
  })
  const items = useFieldArray({ control, name: 'items' })
  const watchedItems = useWatch({ control, name: 'items' })

  const priceById = new Map(products.data?.content.map((product) => [String(product.id), product.price]))
  const total = watchedItems.reduce(
    (sum, item) => sum + (priceById.get(item.productId) ?? 0) * (Number(item.quantity) || 0),
    0,
  )

  function onSubmit(values: OrderFormValues) {
    createOrder.mutate(
      {
        customerId: Number(values.customerId),
        shippingAddress: values.shippingAddress.trim(),
        notes: blankToUndefined(values.notes),
        items: values.items.map((item) => ({ productId: Number(item.productId), quantity: Number(item.quantity) })),
      },
      {
        onSuccess: (order) => {
          toast.success(`Order ${order.orderNumber} created`)
          navigate(`/orders/${order.id}`)
        },
        onError: (error) => {
          if (!applyServerErrors(error, setError)) {
            toast.error(getErrorMessage(error))
          }
        },
      },
    )
  }

  if (customers.isPending || products.isPending) {
    return <Spinner />
  }
  const loadError = customers.error ?? products.error
  if (loadError) {
    const forbidden = loadError instanceof ApiError && loadError.status === 403
    return (
      <ErrorState
        title="Cannot prepare the order form"
        message={
          forbidden
            ? 'To create an order you also need the CUSTOMER_VIEW and PRODUCT_VIEW permissions.'
            : getErrorMessage(loadError)
        }
      />
    )
  }

  return (
    <>
      <PageHeader title="New order" />
      <form className="card form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="form-grid">
          <FormField label="Customer" htmlFor="customerId" error={errors.customerId?.message}>
            <select id="customerId" {...register('customerId', { required: 'Customer id is required' })}>
              <option value="">Select a customer…</option>
              {customers.data?.content.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.firstName} {customer.lastName} ({customer.email})
                </option>
              ))}
            </select>
          </FormField>
        </div>

        <FormField label="Shipping address" htmlFor="shippingAddress" error={errors.shippingAddress?.message}>
          <textarea
            id="shippingAddress"
            rows={2}
            {...register('shippingAddress', {
              required: 'Shipping address is required',
              maxLength: { value: 500, message: 'Shipping address must be at most 500 characters' },
            })}
          />
        </FormField>
        <FormField label="Notes" htmlFor="notes" error={errors.notes?.message}>
          <textarea
            id="notes"
            rows={2}
            {...register('notes', { maxLength: { value: 1000, message: 'Notes must be at most 1000 characters' } })}
          />
        </FormField>

        <div className="section-title">
          <h2>Items</h2>
          <Button onClick={() => items.append(EMPTY_ITEM)}>Add item</Button>
        </div>

        {errors.items?.root?.message && <p className="error-text">{errors.items.root.message}</p>}

        {items.fields.map((field, index) => (
          <div key={field.id} className="item-row">
            <FormField
              label="Product"
              htmlFor={`items.${index}.productId`}
              error={errors.items?.[index]?.productId?.message}
            >
              <select
                id={`items.${index}.productId`}
                {...register(`items.${index}.productId`, { required: 'Product id is required' })}
              >
                <option value="">Select a product…</option>
                {products.data?.content.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} — {formatMoney(product.price)} ({product.stockQuantity} in stock)
                  </option>
                ))}
              </select>
            </FormField>
            <FormField
              label="Quantity"
              htmlFor={`items.${index}.quantity`}
              error={errors.items?.[index]?.quantity?.message}
            >
              <input
                id={`items.${index}.quantity`}
                type="number"
                min="1"
                max="1000"
                {...register(`items.${index}.quantity`, {
                  required: 'Quantity is required',
                  min: { value: 1, message: 'Quantity must be at least 1' },
                  max: { value: 1000, message: 'Quantity must be at most 1000' },
                })}
              />
            </FormField>
            <Button
              variant="ghost"
              disabled={items.fields.length === 1}
              onClick={() => items.remove(index)}
              aria-label={`Remove item ${index + 1}`}
            >
              Remove
            </Button>
          </div>
        ))}

        <p className="total">
          Estimated total: <strong>{formatMoney(total)}</strong>
        </p>

        <div className="row row-end">
          <Button onClick={() => navigate('/orders')}>Cancel</Button>
          <Button type="submit" variant="primary" loading={createOrder.isPending}>
            Place order
          </Button>
        </div>
      </form>
    </>
  )
}
