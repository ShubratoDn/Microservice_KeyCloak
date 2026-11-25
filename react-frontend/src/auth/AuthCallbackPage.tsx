import { useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { useNavigate } from 'react-router-dom'

import { ErrorState } from '@/components/ErrorState'
import { FullPageSpinner } from '@/components/Spinner'

// Keycloak sends the browser back here after login (?code=...&state=...).
// react-oidc-context swaps the code for tokens. Then we go to the page the user wanted.
export function AuthCallbackPage() {
  const auth = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (auth.isAuthenticated) {
      const state = auth.user?.state as { returnTo?: string } | undefined
      navigate(state?.returnTo ?? '/', { replace: true })
    }
  }, [auth.isAuthenticated, auth.user, navigate])

  if (auth.error) {
    return (
      <div className="full-page">
        <ErrorState title="Login failed" message={auth.error.message} onRetry={() => navigate('/', { replace: true })} />
      </div>
    )
  }
  return <FullPageSpinner label="Signing you in…" />
}
