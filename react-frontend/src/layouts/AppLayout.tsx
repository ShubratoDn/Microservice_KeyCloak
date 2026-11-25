import { NavLink, Outlet } from 'react-router-dom'

import { useCurrentUser } from '@/auth/useCurrentUser'
import { Button } from '@/components/Button'

import { NAV_ITEMS } from './navItems'

export function AppLayout() {
  const { username, can, logout } = useCurrentUser()

  return (
    <div className="app">
      <header className="topbar">
        <span className="brand">Learn Microservices</span>
        <nav aria-label="Main">
          {NAV_ITEMS.filter((item) => !item.permission || can(item.permission)).map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="user">
          <span>{username}</span>
          <Button variant="ghost" onClick={() => void logout()}>
            Log out
          </Button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
