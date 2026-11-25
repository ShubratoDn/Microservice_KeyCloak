import { useState } from 'react'
import { Link } from 'react-router-dom'

import { Permissions } from '@/auth/permissions'
import { useCurrentUser } from '@/auth/useCurrentUser'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { PageHeader } from '@/components/PageHeader'
import { Pagination } from '@/components/Pagination'
import { Spinner } from '@/components/Spinner'
import { StatusBadge } from '@/components/StatusBadge'
import { formatDateTime, formatMoney, humanize } from '@/lib/format'

import { useOrders } from './orderHooks'
import { ORDER_STATUSES, type OrderStatus } from './types'

const PAGE_SIZE = 10

export function OrdersPage() {
  const { can } = useCurrentUser()
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<OrderStatus | ''>('')
  const [customerId, setCustomerId] = useState('')

  const orders = useOrders(
    { page, size: PAGE_SIZE },
    { status: status || undefined, customerId: customerId ? Number(customerId) : undefined },
  )

  return (
    <>
      <PageHeader
        title="Orders"
        subtitle="Newest first"
        actions={
          can(Permissions.ORDER_ADD) && (
            <Link className="btn btn-primary" to="/orders/new">
              New order
            </Link>
          )
        }
      />

      <div className="card filters">
        <select
          aria-label="Filter by status"
          value={status}
          onChange={(event) => {
            setStatus(event.target.value as OrderStatus | '')
            setPage(0)
          }}
        >
          <option value="">All statuses</option>
          {ORDER_STATUSES.map((value) => (
            <option key={value} value={value}>
              {humanize(value)}
            </option>
          ))}
        </select>
        <input
          type="number"
          min="1"
          placeholder="Customer id"
          aria-label="Filter by customer id"
          value={customerId}
          onChange={(event) => {
            setCustomerId(event.target.value)
            setPage(0)
          }}
        />
      </div>

      {orders.isPending && <Spinner />}
      {orders.isError && <ErrorState error={orders.error} onRetry={() => void orders.refetch()} />}

      {orders.data && (
        <div className="card">
          {orders.data.content.length === 0 ? (
            <EmptyState message="No orders found." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Order</th>
                    <th>Customer</th>
                    <th>Status</th>
                    <th className="num">Total</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.data.content.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <Link to={`/orders/${order.id}`}>{order.orderNumber}</Link>
                      </td>
                      <td>#{order.customerId}</td>
                      <td>
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="num">{formatMoney(order.totalAmount)}</td>
                      <td>{formatDateTime(order.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <Pagination
            page={orders.data.page}
            totalPages={orders.data.totalPages}
            totalElements={orders.data.totalElements}
            onPageChange={setPage}
          />
        </div>
      )}
    </>
  )
}
