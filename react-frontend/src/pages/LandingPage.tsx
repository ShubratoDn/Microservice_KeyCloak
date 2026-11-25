import { useAuth } from 'react-oidc-context'
import { useLocation } from 'react-router-dom'

import { Button } from '@/components/Button'

// The page everybody sees before they log in.
export function LandingPage() {
  const auth = useAuth()
  const location = useLocation()

  function login() {
    // Remember the page the user wanted, so we can return to it after the login.
    void auth.signinRedirect({ state: { returnTo: `${location.pathname}${location.search}` } })
  }

  return (
    <div className="full-page">
      <section className="landing card">
        <h1>Learn Microservices</h1>
        <p className="muted">Manage customers, products and orders. Sign in with your account to continue.</p>
        <Button variant="primary" className="landing-login" onClick={login}>
          Log in
        </Button>
      </section>
    </div>
  )
}
