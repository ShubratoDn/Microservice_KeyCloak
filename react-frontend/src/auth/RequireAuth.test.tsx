import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'

import { RequireAuth } from './RequireAuth'

const auth = vi.hoisted(() => ({
  current: {} as Record<string, unknown>,
}))

vi.mock('react-oidc-context', () => ({ useAuth: () => auth.current }))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <RequireAuth />
    </MemoryRouter>,
  )
}

describe('<RequireAuth>', () => {
  it('shows the landing page with a login button when the user is not logged in', () => {
    auth.current = { isLoading: false, isAuthenticated: false, signinRedirect: vi.fn() }
    renderAt('/')
    expect(screen.getByRole('button', { name: 'Log in' })).toBeInTheDocument()
  })

  it('starts the login and remembers the wanted page when the button is clicked', async () => {
    const signinRedirect = vi.fn()
    auth.current = { isLoading: false, isAuthenticated: false, signinRedirect }
    renderAt('/orders?page=2')

    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(signinRedirect).toHaveBeenCalledWith({ state: { returnTo: '/orders?page=2' } })
  })

  it('shows a spinner while the login state is loading', () => {
    auth.current = { isLoading: true, isAuthenticated: false }
    renderAt('/')
    expect(screen.queryByRole('button', { name: 'Log in' })).not.toBeInTheDocument()
    expect(screen.getByRole('status')).toBeInTheDocument()
  })
})
