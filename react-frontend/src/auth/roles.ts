import { jwtDecode } from 'jwt-decode'

import type { Permission } from './permissions'

interface KeycloakClaims {
  realm_access?: { roles?: string[] }
}

// Keycloak puts the realm roles inside the access token: realm_access.roles.
// We only READ the token here (no signature check). That is fine for showing or
// hiding buttons. The backend validates the signature and enforces the roles.
export function extractRealmRoles(accessToken: string | undefined): string[] {
  if (!accessToken) {
    return []
  }
  try {
    return jwtDecode<KeycloakClaims>(accessToken).realm_access?.roles ?? []
  } catch {
    return []
  }
}

export function hasPermission(roles: readonly string[], permission: Permission): boolean {
  return roles.includes(permission)
}
