import { createBrowserRouter } from 'react-router-dom';
import { AdminLayout } from './layouts/admin-layout';
import { PrivateRoute } from './common/components/private-route';
import { LoginPage } from './features/auth/components/login-page';
import { DashboardPage } from './features/dashboard/components/dashboard-page';
import { BudgetPage } from './features/budget/components/budget-page';
import { ProductPage } from './features/product/components/product-page';
import { OrderPage } from './features/order/components/order-page';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: (
      <PrivateRoute>
        <AdminLayout />
      </PrivateRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'budget', element: <BudgetPage /> },
      { path: 'products', element: <ProductPage /> },
      { path: 'orders', element: <OrderPage /> },
    ],
  },
]);
