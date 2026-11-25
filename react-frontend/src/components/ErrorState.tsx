import { getErrorMessage } from '@/api/ApiError'

import { Button } from './Button'

interface ErrorStateProps {
  title?: string
  error?: unknown
  message?: string
  onRetry?: () => void
}

export function ErrorState({ title = 'Something went wrong', error, message, onRetry }: ErrorStateProps) {
  return (
    <div className="card state state-error" role="alert">
      <h2>{title}</h2>
      <p>{message ?? getErrorMessage(error)}</p>
      {onRetry && <Button onClick={onRetry}>Try again</Button>}
    </div>
  )
}
