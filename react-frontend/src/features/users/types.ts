export const USER_TYPES = ['ADMIN', 'MANAGER', 'STAFF'] as const
export type UserType = (typeof USER_TYPES)[number]

export const GENDERS = ['MALE', 'FEMALE', 'OTHER'] as const
export type Gender = (typeof GENDERS)[number]

export const RELIGIONS = [
  'ISLAM',
  'HINDUISM',
  'BUDDHISM',
  'CHRISTIANITY',
  'JUDAISM',
  'SIKHISM',
  'OTHER',
  'PREFER_NOT_TO_SAY',
] as const
export type Religion = (typeof RELIGIONS)[number]

export interface UserSummary {
  id: number
  firstName: string
  lastName: string | null
  email: string
  username: string | null
  phoneNumber: string
  userType: UserType
  enabled: boolean
}

export interface User extends UserSummary {
  keycloakId: string
  presentAddress: string
  permanentAddress: string | null
  gender: Gender
  religion: Religion | null
  createdBy: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  firstName: string
  lastName?: string
  email: string
  username?: string
  phoneNumber: string
  presentAddress: string
  permanentAddress?: string
  gender: Gender
  religion?: Religion
  userType: UserType
  password: string
}

export type UpdateUserRequest = Omit<CreateUserRequest, 'email' | 'username' | 'password'>

export interface UserFilters {
  userType?: UserType
  q?: string
}
