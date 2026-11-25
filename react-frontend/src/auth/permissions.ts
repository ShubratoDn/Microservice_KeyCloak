// These names must match the realm roles in Keycloak and the constants in the
// backend (Permissions.java). The UI uses them only to show or hide buttons.
// The real protection is always done by the backend.
export const Permissions = {
  CUSTOMER_ADD: 'CUSTOMER_ADD',
  CUSTOMER_VIEW: 'CUSTOMER_VIEW',
  CUSTOMER_EDIT: 'CUSTOMER_EDIT',
  CUSTOMER_DELETE: 'CUSTOMER_DELETE',
  PRODUCT_ADD: 'PRODUCT_ADD',
  PRODUCT_VIEW: 'PRODUCT_VIEW',
  PRODUCT_EDIT: 'PRODUCT_EDIT',
  PRODUCT_DELETE: 'PRODUCT_DELETE',
  ORDER_ADD: 'ORDER_ADD',
  ORDER_VIEW: 'ORDER_VIEW',
  ORDER_EDIT: 'ORDER_EDIT',
  ORDER_DELETE: 'ORDER_DELETE',
  USER_ADD: 'USER_ADD',
  USER_VIEW: 'USER_VIEW',
  USER_EDIT: 'USER_EDIT',
  USER_DELETE: 'USER_DELETE',
} as const

export type Permission = (typeof Permissions)[keyof typeof Permissions]
