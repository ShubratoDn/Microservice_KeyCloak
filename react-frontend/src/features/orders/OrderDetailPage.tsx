import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { getErrorMessage } from '@/api/ApiError'
import { Permissions } from '@/auth/permissions'
import { useCurrentUser } from '@/auth/useCurrentUser'
import { Button } from '@/components/Button'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { ErrorState } from '@/components/ErrorState'
import { PageHeader } from '@/components/PageHeader'
import { Spinner } from '@/components/Spinner'
import { StatusBadge } from '@/components/StatusBadge'
import { useToast } from '@/components/toast/toastContext'
import { formatDateTime, formatMoney, humanize } from '@/lib/format'

import { useOrder, useUpdateOrderStatus } from './orderHooks'
import { nextStatuses } from './orderStatus'
import type { OrderStatus } from './types'

export function OrderDetailPage() {
  const orderId = Number(useParams().id)
  const { can } = useCurrentUser()
  const toast = useToast()
  const [pendingStatus, setPendingStatus] = useState<OrderStatus | null>(null)

  const order = useOrder(orderId)
  const updateStatus = useUpdateOrderStatus(orderId)

  if (order.isPending) {
    return <Spinner />
  }
  if (order.isError) {
    return <ErrorState error={order.error} onRetry={() => void order.refetch()} />
  }

  const data = order.data
  const actions = can(Permissions.ORDER_EDIT) ? nextStatuses(data.status) : []

  function confirmStatusChange() {
    if (!pendingStatus) {
      return
    }
    updateStatus.mutate(pendingStatus, {
      onSuccess: () => toast.success(`Order is now ${humanize(pendingStatus)}`),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setPendingStatus(null),
    })
  }

  return (
    <>
      <PageHeader
        title={`Order ${data.orderNumber}`}
        subtitle={`Created ${formatDateTime(data.createdAt)}`}
        actions={
          <>
            <Link className="btn btn-secondary" to="/orders">
              Back to orders
            </Link>
            {actions.map((status) => (
              <Button
                key={status}
                variant={status === 'CANCELLED' ? 'danger' : 'primary'}
                onClick={() => setPendingStatus(status)}
              >
                {status === 'CANCELLED' ? 'Cancel order' : `Mark as ${humanize(status)}`}
              </Button>
            ))}
          </>
        }
      />

      <div className="card">
        <dl className="details">
          <div>
            <dt>Status</dt>
            <dd>
              <StatusBadge status={data.status} />
            </dd>
          </div>
          <div>
            <dt>Customer</dt>
            <dd>#{data.customerId}</dd>
          </div>
          <div>
            <dt>Total</dt>
            <dd>{formatMoney(data.totalAmount)}</dd>
          </div>
          <div>
            <dt>Shipping address</dt>
            <dd>{data.shippingAddress}</dd>
          </div>
          <div>
            <dt>Notes</dt>
            <dd>{data.notes ?? '—'}</dd>
          </div>
        </dl>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Product</th>
                <th>SKU</th>
                <th className="num">Unit price</th>
                <th className="num">Quantity</th>
                <th className="num">Line total</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((item) => (
                <tr key={item.id}>
                  <td>{item.productName}</td>
                  <td>
                    <code>{item.sku}</code>
                  </td>
                  <td className="num">{formatMoney(item.unitPrice)}</td>
                  <td className="num">{item.quantity}</td>
                  <td className="num">{formatMoney(item.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <ConfirmDialog
        open={pendingStatus !== null}
        danger={pendingStatus === 'CANCELLED'}
        title="Change order status"
        message={`Change the status of ${data.orderNumber} to ${humanize(pendingStatus ?? '')}?`}
        confirmLabel="Yes, change it"
        loading={updateStatus.isPending}
        onConfirm={confirmStatusChange}
        onCancel={() => setPendingStatus(null)}
      />
    </>
  )
}
