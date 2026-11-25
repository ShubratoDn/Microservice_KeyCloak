import { Link } from 'react-router-dom'

import { useCurrentUser } from '@/auth/useCurrentUser'
import { PageHeader } from '@/components/PageHeader'
import { NAV_ITEMS } from '@/layouts/navItems'

// Roles that start with one of these words are shown. Keycloak adds a few
// built-in roles (offline_access, uma_authorization ...) that we do not need to show.
const APP_ROLE = /^(CUSTOMER|PRODUCT|ORDER|USER)_/

export function DashboardPage() {
  const { fullName, username, email, roles, can } = useCurrentUser()
  const appRoles = roles.filter((role) => APP_ROLE.test(role)).sort()
  const modules = NAV_ITEMS.filter((item) => item.to !== '/' && item.permission && can(item.permission))

  return (
    <>
      <PageHeader title={`Welcome, ${fullName ?? username}`} subtitle={email} />

      <div className="card">
        <h2>Your permissions</h2>
        {appRoles.length === 0 ? (
          <p className="muted">You have no permissions yet. Ask an administrator to give you roles in Keycloak.</p>
        ) : (
          <ul className="chips">
            {appRoles.map((role) => (
              <li key={role}>{role}</li>
            ))}
          </ul>
        )}
      </div>

      {modules.length > 0 && (
        <div className="tiles">
          {modules.map((item) => (
            <Link key={item.to} className="tile" to={item.to}>
              {item.label}
            </Link>
          ))}
        </div>
      )}
    </>
  )
}
