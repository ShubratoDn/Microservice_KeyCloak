import { useState } from 'react'
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
import { humanize } from '@/lib/format'

import { useCustomers, useDeleteCustomer, useUpdateCustomerStatus } from './customerHooks'
import { CUSTOMER_STATUSES, type CustomerStatus, type CustomerSummary } from './types'

const PAGE_SIZE = 10

export function CustomersPage() {
  const { can } = useCurrentUser()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [toDelete, setToDelete] = useState<CustomerSummary | null>(null)

  const customers = useCustomers({ page, size: PAGE_SIZE })
  const updateStatus = useUpdateCustomerStatus()
  const deleteCustomer = useDeleteCustomer()

  function changeStatus(customer: CustomerSummary, status: CustomerStatus) {
    updateStatus.mutate(
      { id: customer.id, status },
      {
        onSuccess: () => toast.success(`${customer.firstName} is now ${humanize(status)}`),
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  function confirmDelete() {
    if (!toDelete) {
      return
    }
    deleteCustomer.mutate(toDelete.id, {
      onSuccess: () => {
        toast.success('Customer deleted')
        setToDelete(null)
      },
      onError: (error) => {
        toast.error(getErrorMessage(error))
        setToDelete(null)
      },
    })
  }

  return (
    <>
      <PageHeader
        title="Customers"
        subtitle="People who can place orders"
        actions={
          can(Permissions.CUSTOMER_ADD) && (
            <Link className="btn btn-primary" to="/customers/new">
              New customer
            </Link>
          )
        }
      />

      {customers.isPending && <Spinner />}
      {customers.isError && <ErrorState error={customers.error} onRetry={() => void customers.refetch()} />}

      {customers.data && (
        <div className="card">
          {customers.data.content.length === 0 ? (
            <EmptyState message="No customers yet." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Status</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {customers.data.content.map((customer) => (
                    <tr key={customer.id}>
                      <td>
                        {customer.firstName} {customer.lastName}
                      </td>
                      <td>{customer.email}</td>
                      <td>{customer.phoneNumber ?? '—'}</td>
                      <td>
                        {can(Permissions.CUSTOMER_EDIT) ? (
                          <select
                            aria-label={`Status of ${customer.firstName} ${customer.lastName}`}
                            value={customer.status}
                            disabled={updateStatus.isPending}
                            onChange={(event) => changeStatus(customer, event.target.value as CustomerStatus)}
                          >
                            {CUSTOMER_STATUSES.map((status) => (
                              <option key={status} value={status}>
                                {humanize(status)}
                              </option>
                            ))}
                          </select>
                        ) : (
                          <StatusBadge status={customer.status} />
                        )}
                      </td>
                      <td className="row row-end">
                        {can(Permissions.CUSTOMER_EDIT) && (
                          <Link className="btn btn-secondary" to={`/customers/${customer.id}/edit`}>
                            Edit
                          </Link>
                        )}
                        {can(Permissions.CUSTOMER_DELETE) && (
                          <Button variant="danger" onClick={() => setToDelete(customer)}>
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
            page={customers.data.page}
            totalPages={customers.data.totalPages}
            totalElements={customers.data.totalElements}
            onPageChange={setPage}
          />
        </div>
      )}

      <ConfirmDialog
        open={toDelete !== null}
        danger
        title="Delete customer"
        message={`Delete ${toDelete?.firstName ?? ''} ${toDelete?.lastName ?? ''}? This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleteCustomer.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  )
}
