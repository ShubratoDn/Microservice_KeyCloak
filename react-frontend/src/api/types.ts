// Same shape as PagedResponse in the Java services.
export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PageParams {
  page: number
  size: number
}

// The error body of the backend (Spring "ProblemDetail").
export interface ProblemDetail {
  title?: string
  status?: number
  detail?: string
  errors?: Record<string, string>
}
