import { UserManager, WebStorageStateStore } from 'oidc-client-ts'

import { env } from '@/config/env'

// One shared UserManager. It does the OAuth 2.0 "Authorization Code + PKCE" login
// with Keycloak. We keep it outside React so the API client can also read the token.
export const userManager = new UserManager({
  authority: `${env.keycloakUrl}/realms/${env.keycloakRealm}`,
  client_id: env.keycloakClientId,
  redirect_uri: `${window.location.origin}/auth/callback`,
  post_logout_redirect_uri: `${window.location.origin}/`,
  response_type: 'code',
  scope: 'openid profile email',
  // Get a new access token shortly before the old one expires.
  automaticSilentRenew: true,
  // sessionStorage: the login is forgotten when the tab is closed.
  userStore: new WebStorageStateStore({ store: window.sessionStorage }),
})
