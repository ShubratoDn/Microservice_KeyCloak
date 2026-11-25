import type { ReactNode } from 'react'

import type { Permission } from '@/auth/permissions'
import { useCurrentUser } from '@/auth/useCurrentUser'

interface CanProps {
  permission: Permission
  children: ReactNode
}

// Shows the children only if the user has the permission (Keycloak role).
export function Can({ permission, children }: CanProps) {
  const { can } = useCurrentUser()
  return can(permission) ? <>{children}</> : null
}
