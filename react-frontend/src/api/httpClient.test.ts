
import { ApiError } from './ApiError'

const getUser = vi.fn()
vi.mock('@/auth/userManager', () => ({ userManager: { getUser } }))

const { http } = await import('./httpClient')

function jsonResponse(status: number, body?: unknown): Response {
  return new Response(body === undefined ? null : JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('http client', () => {
  const fetchMock = vi.fn()

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock)
    getUser.mockResolvedValue({ access_token: 'my-token', expired: false })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.clearAllMocks()
  })

  it('sends the access token as a Bearer header', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, { ok: true }))

    await http.get('/api/v1/orders')

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer my-token')
  })

  it('does not send a token when the user is not logged in', async () => {
    getUser.mockResolvedValue(null)
    fetchMock.mockResolvedValue(jsonResponse(200, {}))

    await http.get('/api/v1/orders')

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toBeUndefined()
  })

  it('builds the query string and skips empty values', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, {}))

    await http.get('/api/v1/products', { query: { page: 0, size: 10, q: '', status: undefined, categoryId: 3 } })

    expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/products?page=0&size=10&categoryId=3')
  })

  it('turns an error response into an ApiError with field errors', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(400, { title: 'Validation Error', detail: 'Request validation failed', errors: { email: 'Email is required' } }),
    )

    const error = await http.post('/api/v1/customers', {}).catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).status).toBe(400)
    expect((error as ApiError).fieldErrors).toEqual({ email: 'Email is required' })
  })

  it('returns undefined for 204 No Content', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))

    await expect(http.delete('/api/v1/customers/1')).resolves.toBeUndefined()
  })
})
