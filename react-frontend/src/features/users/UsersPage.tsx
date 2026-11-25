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
import { humanize } from '@/lib/format'

import { USER_TYPES, type UserSummary, type UserType } from './types'
import { useDeleteUser, useUpdateUserStatus, useUsers } from './userHooks'

const PAGE_SIZE = 10

export function UsersPage() {
  const { can } = useCurrentUser()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [userType, setUserType] = useState<UserType | ''>('')
  const [toDelete, setToDelete] = useState<UserSummary | null>(null)

  const deferredSearch = useDeferredValue(search.trim())
  const users = useUsers({ page, size: PAGE_SIZE }, { q: deferredSearch || undefined, userType: userType || undefined })
  const updateStatus = useUpdateUserStatus()
  const deleteUser = useDeleteUser()

  function toggleEnabled(user: UserSummary) {
    updateStatus.mutate(
      { id: user.id, enabled: !user.enabled },
      {
        onSuccess: () => toast.success(`${user.firstName} is now ${user.enabled ? 'disabled' : 'enabled'}`),
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  function confirmDelete() {
    if (!toDelete) {
      return
    }
    deleteUser.mutate(toDelete.id, {
      onSuccess: () => toast.success('User deleted'),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setToDelete(null),
    })
  }

  return (
    <>
      <PageHeader
        title="Users"
        subtitle="People who can log in: admins, managers and staff"
        actions={
          can(Permissions.USER_ADD) && (
            <Link className="btn btn-primary" to="/users/new">
              Register user
            </Link>
          )
        }
      />

      <div className="card filters">
        <input
          type="search"
          placeholder="Search by name, username or email…"
          aria-label="Search users"
          value={search}
          onChange={(event) => {
            setSearch(event.target.value)
            setPage(0)
          }}
        />
        <select
          aria-label="Filter by user type"
          value={userType}
          onChange={(event) => {
            setUserType(event.target.value as UserType | '')
            setPage(0)
          }}
        >
          <option value="">All user types</option>
          {USER_TYPES.map((type) => (
            <option key={type} value={type}>
              {humanize(type)}
            </option>
          ))}
        </select>
      </div>

      {users.isPending && <Spinner />}
      {users.isError && <ErrorState error={users.error} onRetry={() => void users.refetch()} />}

      {users.data && (
        <div className="card">
          {users.data.content.length === 0 ? (
            <EmptyState message="No users found." />
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Type</th>
                    <th>Account</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {users.data.content.map((user) => (
                    <tr key={user.id}>
                      <td>
                        {user.firstName} {user.lastName}
                      </td>
                      <td>{user.username ?? user.email}</td>
                      <td>{user.email}</td>
                      <td>{user.phoneNumber}</td>
                      <td>
                        <StatusBadge status={user.userType} />
                      </td>
                      <td>
                        <StatusBadge status={user.enabled ? 'ACTIVE' : 'INACTIVE'} />
                      </td>
                      <td className="row row-end">
                        {can(Permissions.USER_EDIT) && (
                          <>
                            <Button disabled={updateStatus.isPending} onClick={() => toggleEnabled(user)}>
                              {user.enabled ? 'Disable' : 'Enable'}
                            </Button>
                            <Link className="btn btn-secondary" to={`/users/${user.id}/edit`}>
                              Edit
                            </Link>
                          </>
                        )}
                        {can(Permissions.USER_DELETE) && (
                          <Button variant="danger" onClick={() => setToDelete(user)}>
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
            page={users.data.page}
            totalPages={users.data.totalPages}
            totalElements={users.data.totalElements}
            onPageChange={setPage}
          />
        </div>
      )}

      <ConfirmDialog
        open={toDelete !== null}
        danger
        title="Delete user"
        message={`Delete ${toDelete?.firstName ?? ''} ${toDelete?.lastName ?? ''}? Their login account is removed too. This cannot be undone.`}
        confirmLabel="Delete"
        loading={deleteUser.isPending}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  )
}
