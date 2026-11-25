import { QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AuthProvider } from 'react-oidc-context'

import { queryClient } from '@/api/queryClient'
import { userManager } from '@/auth/userManager'
import { ToastProvider } from '@/components/toast/ToastProvider'

export function Providers({ children }: { children: ReactNode }) {
  return (
    <AuthProvider
      userManager={userManager}
      // After login, remove "?code=...&state=..." from the address bar.
      onSigninCallback={() => window.history.replaceState({}, document.title, '/auth/callback')}
    >
      <QueryClientProvider client={queryClient}>
        <ToastProvider>{children}</ToastProvider>
      </QueryClientProvider>
    </AuthProvider>
  )
}
