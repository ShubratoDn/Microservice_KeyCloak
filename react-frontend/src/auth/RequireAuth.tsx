import { useAuth } from 'react-oidc-context'
import { Outlet } from 'react-router-dom'

import { ErrorState } from '@/components/ErrorState'
import { FullPageSpinner } from '@/components/Spinner'
import { LandingPage } from '@/pages/LandingPage'

// Wraps every page that needs a login.
// Not logged in: show the landing page with a "Log in" button.
// The user clicks it, logs in at Keycloak, and comes back to the page they wanted.
export function RequireAuth() {
  const auth = useAuth()

  if (auth.isLoading || auth.activeNavigator) {
    return <FullPageSpinner label="Please wait…" />
  }
  if (auth.error) {
    return (
      <div className="full-page">
        <ErrorState
          title="Login failed"
          message={auth.error.message}
          onRetry={() => void auth.signinRedirect({ state: { returnTo: '/' } })}
        />
      </div>
    )
  }
  if (!auth.isAuthenticated) {
    return <LandingPage />
  }
  return <Outlet />
}
