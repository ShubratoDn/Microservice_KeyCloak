function required(name: 'VITE_KEYCLOAK_URL' | 'VITE_KEYCLOAK_REALM' | 'VITE_KEYCLOAK_CLIENT_ID'): string {
  const value = import.meta.env[name]
  if (!value) {
    throw new Error(`Missing environment variable ${name}. Copy .env.example to .env.development.`)
  }
  return value
}

export const env = {
  keycloakUrl: required('VITE_KEYCLOAK_URL'),
  keycloakRealm: required('VITE_KEYCLOAK_REALM'),
  keycloakClientId: required('VITE_KEYCLOAK_CLIENT_ID'),
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '',
} as const
