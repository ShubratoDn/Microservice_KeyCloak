import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query'

import { userManager } from '@/auth/userManager'

import { ApiError } from './ApiError'

// If the backend says "401" the token is no longer valid: log in again.
function handleUnauthorized(error: unknown) {
  if (error instanceof ApiError && error.status === 401) {
    void userManager.removeUser().then(() => userManager.signinRedirect())
  }
}

export const queryClient = new QueryClient({
  queryCache: new QueryCache({ onError: handleUnauthorized }),
  mutationCache: new MutationCache({ onError: handleUnauthorized }),
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Retrying a 401/403/404 never helps. Retry other errors up to 2 times.
      retry: (failureCount, error) =>
        !(error instanceof ApiError && [400, 401, 403, 404, 409].includes(error.status)) && failureCount < 2,
    },
  },
})
