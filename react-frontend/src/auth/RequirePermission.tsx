import type { ReactNode } from 'react'
import { Link, Outlet } from 'react-router-dom'

import type { Permission } from './permissions'
import { useCurrentUser } from './useCurrentUser'

interface RequirePermissionProps {
  permission: Permission
  children?: ReactNode
}

// Route guard. Used as a layout route (renders <Outlet />) or around children.
export function RequirePermission({ permission, children }: RequirePermissionProps) {
  const { can } = useCurrentUser()

  if (!can(permission)) {
    return (
      <div className="card state">
        <h2>Access denied</h2>
        <p>
          You need the <code>{permission}</code> permission to open this page.
        </p>
        <Link to="/">Back to the dashboard</Link>
      </div>
    )
  }
  return children ?? <Outlet />
}
