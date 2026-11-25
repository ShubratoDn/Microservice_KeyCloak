import { render, screen } from '@testing-library/react'

import { Permissions } from '@/auth/permissions'

import { Can } from './Can'

const roles = vi.hoisted(() => ({ current: [] as string[] }))

vi.mock('@/auth/useCurrentUser', () => ({
  useCurrentUser: () => ({ can: (permission: string) => roles.current.includes(permission) }),
}))

describe('<Can>', () => {
  it('shows its children when the user has the permission', () => {
    roles.current = [Permissions.ORDER_ADD]
    render(
      <Can permission={Permissions.ORDER_ADD}>
        <button>New order</button>
      </Can>,
    )
    expect(screen.getByRole('button', { name: 'New order' })).toBeInTheDocument()
  })

  it('hides its children when the permission is missing', () => {
    roles.current = [Permissions.ORDER_VIEW]
    render(
      <Can permission={Permissions.ORDER_ADD}>
        <button>New order</button>
      </Can>,
    )
    expect(screen.queryByRole('button', { name: 'New order' })).not.toBeInTheDocument()
  })
})
