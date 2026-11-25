import { Permissions, type Permission } from '@/auth/permissions'

interface NavItem {
  to: string
  label: string
  permission?: Permission
}

export const NAV_ITEMS: readonly NavItem[] = [
  { to: '/', label: 'Dashboard' },
  { to: '/customers', label: 'Customers', permission: Permissions.CUSTOMER_VIEW },
  { to: '/products', label: 'Products', permission: Permissions.PRODUCT_VIEW },
  { to: '/categories', label: 'Categories', permission: Permissions.PRODUCT_VIEW },
  { to: '/orders', label: 'Orders', permission: Permissions.ORDER_VIEW },
]
