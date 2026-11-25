
import { Permissions } from './permissions'
import { extractRealmRoles, hasPermission } from './roles'

function fakeToken(payload: object): string {
  const encode = (value: object) => btoa(JSON.stringify(value)).replaceAll('=', '').replaceAll('+', '-').replaceAll('/', '_')
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`
}

describe('extractRealmRoles', () => {
  it('reads the realm roles from the token', () => {
    const token = fakeToken({ realm_access: { roles: ['ORDER_VIEW', 'ORDER_ADD'] } })
    expect(extractRealmRoles(token)).toEqual(['ORDER_VIEW', 'ORDER_ADD'])
  })

  it('returns an empty list when the token has no realm_access', () => {
    expect(extractRealmRoles(fakeToken({ sub: 'user' }))).toEqual([])
  })

  it('returns an empty list when there is no token or it is broken', () => {
    expect(extractRealmRoles(undefined)).toEqual([])
    expect(extractRealmRoles('not-a-jwt')).toEqual([])
  })
})

describe('hasPermission', () => {
  it('is true only for roles the user has', () => {
    expect(hasPermission(['ORDER_VIEW'], Permissions.ORDER_VIEW)).toBe(true)
    expect(hasPermission(['ORDER_VIEW'], Permissions.ORDER_ADD)).toBe(false)
  })
})
