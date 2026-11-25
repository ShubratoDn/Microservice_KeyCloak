export const CUSTOMER_STATUSES = ['ACTIVE', 'INACTIVE', 'BLOCKED'] as const
export type CustomerStatus = (typeof CUSTOMER_STATUSES)[number]

export const ADDRESS_TYPES = ['HOME', 'WORK', 'BILLING', 'SHIPPING'] as const
export type AddressType = (typeof ADDRESS_TYPES)[number]

export interface Address {
  id: number
  type: AddressType
  street: string
  city: string
  state: string | null
  postalCode: string
  country: string
}

export interface CustomerSummary {
  id: number
  firstName: string
  lastName: string
  email: string
  phoneNumber: string | null
  status: CustomerStatus
}

export interface Customer extends CustomerSummary {
  addresses: Address[]
  createdAt: string
  updatedAt: string
}

export interface AddressRequest {
  type: AddressType
  street: string
  city: string
  state?: string
  postalCode: string
  country: string
}

export interface CustomerRequest {
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
  addresses: AddressRequest[]
}
