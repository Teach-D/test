import { type ReactElement } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LoginPage } from './features/auth/components/login-page';
import { PrivateRoute } from './common/components/private-route';
import { Layout } from './common/components/layout';
import { RoulettePage } from './features/roulette/components/roulette-page';
import { PointPage } from './features/point/components/point-page';
import { ProductPage } from './features/product/components/product-page';

// 플레이스홀더 컴포넌트들 (나중에 실제 페이지로 교체)

function OrdersPage(): ReactElement {
  return (
    <div className="flex h-full items-center justify-center p-4">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900">주문</h2>
        <p className="mt-2 text-gray-600">주문 내역이 여기에 표시됩니다</p>
      </div>
    </div>
  );
}

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
            element: <OrdersPage />,
          },
        ],
      },
    ],
  },
]);

export function Router(): ReactElement {
  return <RouterProvider router={router} />;
}
