import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'

import { ApiError } from '@/api/ApiError'

// The backend returns { errors: { "addresses[0].street": "Street is required" } }.
// This puts each message under the matching form field.
// It returns true when at least one field message was shown.
export function applyServerErrors<T extends FieldValues>(error: unknown, setError: UseFormSetError<T>): boolean {
  if (!(error instanceof ApiError)) {
    return false
  }
  const entries = Object.entries(error.fieldErrors)
  for (const [field, message] of entries) {
    setError(field.replace(/\[(\d+)\]/g, '.$1') as Path<T>, { type: 'server', message })
  }
  return entries.length > 0
}

// Empty text boxes should be sent as "not provided", not as "".
export function blankToUndefined(value: string | undefined): string | undefined {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}
