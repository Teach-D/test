import { type ReactElement } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LoginPage } from './features/auth/components/login-page';
import { PrivateRoute } from './common/components/private-route';
import { Layout } from './common/components/layout';
import { RoulettePage } from './features/roulette/components/roulette-page';
import { PointPage } from './features/point/components/point-page';
import { ProductPage } from './features/product/components/product-page';
import { OrderPage } from './features/order/components/order-page';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <PrivateRoute />,
    children: [
      {
        element: <Layout />,
        children: [
          {
            index: true,
            element: <RoulettePage />,
          },
          {
            path: 'points',
            element: <PointPage />,
          },
          {
            path: 'products',
            element: <ProductPage />,
          },
          {
            path: 'orders',
            element: <OrderPage />,
          },
        ],
      },
    ],
  },
]);

export function Router(): ReactElement {
  return <RouterProvider router={router} />;
}
