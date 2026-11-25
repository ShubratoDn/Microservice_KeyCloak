import { useMemo } from 'react'
import { useAuth } from 'react-oidc-context'

import type { Permission } from './permissions'
import { extractRealmRoles, hasPermission } from './roles'

export function useCurrentUser() {
  const auth = useAuth()
  const accessToken = auth.user?.access_token
  const roles = useMemo(() => extractRealmRoles(accessToken), [accessToken])
  const profile = auth.user?.profile

  return {
    username: profile?.preferred_username ?? profile?.name ?? 'user',
    fullName: profile?.name,
    email: profile?.email,
    roles,
    can: (permission: Permission) => hasPermission(roles, permission),
    logout: () => auth.signoutRedirect(),
  }
}
