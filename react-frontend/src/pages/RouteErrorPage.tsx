import { useRouteError } from 'react-router-dom'

import { ErrorState } from '@/components/ErrorState'

// Shown when a page crashes while rendering, so the user never sees a blank screen.
export function RouteErrorPage() {
  const error = useRouteError()
  return (
    <div className="full-page">
      <ErrorState title="This page crashed" error={error} onRetry={() => window.location.reload()} />
    </div>
  )
}
