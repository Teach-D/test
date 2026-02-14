import { type ReactElement } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../stores/auth-store';

export function Layout(): ReactElement {
  const location = useLocation();
  const { nickname, logout } = useAuthStore();

  const handleLogout = (): void => {
    logout();
    window.location.href = '/login';
  };

  const isActive = (path: string): boolean => {
    return location.pathname === path;
  };

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-gray-50">
      {/* Header */}
      <header className="fixed left-0 right-0 top-0 z-10 mx-auto max-w-md border-b border-gray-200 bg-white">
        <div className="flex items-center justify-between px-4 py-3">
          <h1 className="text-lg font-bold text-gray-900">포인트 룰렛</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">{nickname}</span>
            <button
              onClick={handleLogout}
              className="text-sm text-indigo-600 hover:text-indigo-700"
            >
              로그아웃
            </button>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 pb-16 pt-14">
        <Outlet />
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 z-10 mx-auto max-w-md border-t border-gray-200 bg-white">
        <div className="flex items-center justify-around">
          <Link
            to="/"
            className={`flex flex-1 flex-col items-center gap-1 py-2 ${
              isActive('/')
                ? 'text-indigo-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            <span className="text-2xl">🎰</span>
            <span className="text-xs font-medium">홈</span>
          </Link>
          <Link
            to="/points"
            className={`flex flex-1 flex-col items-center gap-1 py-2 ${
              isActive('/points')
                ? 'text-indigo-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            <span className="text-2xl">💰</span>
            <span className="text-xs font-medium">포인트</span>
          </Link>
          <Link
            to="/products"
            className={`flex flex-1 flex-col items-center gap-1 py-2 ${
              isActive('/products')
                ? 'text-indigo-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            <span className="text-2xl">🎁</span>
            <span className="text-xs font-medium">상품</span>
          </Link>
          <Link
            to="/orders"
            className={`flex flex-1 flex-col items-center gap-1 py-2 ${
              isActive('/orders')
                ? 'text-indigo-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            <span className="text-2xl">📋</span>
            <span className="text-xs font-medium">주문</span>
          </Link>
        </div>
      </nav>
    </div>
  );
}
