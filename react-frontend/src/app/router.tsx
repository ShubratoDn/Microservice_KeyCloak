import type { ComponentType } from 'react'
import { createBrowserRouter } from 'react-router-dom'

import { AuthCallbackPage } from '@/auth/AuthCallbackPage'
import { Permissions } from '@/auth/permissions'
import { RequireAuth } from '@/auth/RequireAuth'
import { RequirePermission } from '@/auth/RequirePermission'
import { AppLayout } from '@/layouts/AppLayout'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { RouteErrorPage } from '@/pages/RouteErrorPage'

// Each page is loaded only when the user opens it (smaller first download).
function lazyPage<K extends string>(load: () => Promise<Record<K, ComponentType>>, name: K) {
  return async () => ({ Component: (await load())[name] })
}

export const router = createBrowserRouter([
  // Keycloak sends the user back here after login. It must NOT be behind RequireAuth.
  { path: '/auth/callback', element: <AuthCallbackPage /> },
  {
    element: <RequireAuth />,
    errorElement: <RouteErrorPage />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, lazy: lazyPage(() => import('@/pages/DashboardPage'), 'DashboardPage') },
          {
            path: 'customers',
            element: <RequirePermission permission={Permissions.CUSTOMER_VIEW} />,
            children: [
              { index: true, lazy: lazyPage(() => import('@/features/customers/CustomersPage'), 'CustomersPage') },
              {
                path: 'new',
                element: <RequirePermission permission={Permissions.CUSTOMER_ADD} />,
                children: [
                  { index: true, lazy: lazyPage(() => import('@/features/customers/CustomerFormPage'), 'CustomerFormPage') },
                ],
              },
              {
                path: ':id/edit',
                element: <RequirePermission permission={Permissions.CUSTOMER_EDIT} />,
                children: [
                  { index: true, lazy: lazyPage(() => import('@/features/customers/CustomerFormPage'), 'CustomerFormPage') },
                ],
              },
            ],
          },
          {
            path: 'products',
            element: <RequirePermission permission={Permissions.PRODUCT_VIEW} />,
            children: [
              { index: true, lazy: lazyPage(() => import('@/features/products/ProductsPage'), 'ProductsPage') },
              {
                path: 'new',
                element: <RequirePermission permission={Permissions.PRODUCT_ADD} />,
                children: [
                  { index: true, lazy: lazyPage(() => import('@/features/products/ProductFormPage'), 'ProductFormPage') },
                ],
              },
              {
                path: ':id/edit',
                element: <RequirePermission permission={Permissions.PRODUCT_EDIT} />,
                children: [
                  { index: true, lazy: lazyPage(() => import('@/features/products/ProductFormPage'), 'ProductFormPage') },
                ],
              },
            ],
          },
          {
            path: 'categories',
            element: <RequirePermission permission={Permissions.PRODUCT_VIEW} />,
            children: [
              { index: true, lazy: lazyPage(() => import('@/features/categories/CategoriesPage'), 'CategoriesPage') },
            ],
          },
          {
            path: 'orders',
            element: <RequirePermission permission={Permissions.ORDER_VIEW} />,
            children: [
              { index: true, lazy: lazyPage(() => import('@/features/orders/OrdersPage'), 'OrdersPage') },
              {
                path: 'new',
                element: <RequirePermission permission={Permissions.ORDER_ADD} />,
                children: [
                  { index: true, lazy: lazyPage(() => import('@/features/orders/OrderCreatePage'), 'OrderCreatePage') },
                ],
              },
              { path: ':id', lazy: lazyPage(() => import('@/features/orders/OrderDetailPage'), 'OrderDetailPage') },
            ],
          },
          { path: '*', element: <NotFoundPage /> },
        ],
      },
    ],
  },
])
