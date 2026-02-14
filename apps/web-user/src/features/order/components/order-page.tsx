import { type ReactElement, useEffect, useState } from 'react';
import dayjs from 'dayjs';
import { getMyOrders } from '../api/order-api';
import type { OrderResponse } from '@/common/types/api';
import { LoadingSpinner } from '@/common/components/loading-spinner';
import { ErrorMessage } from '@/common/components/error-message';

type OrderStatus = 'COMPLETED' | 'CANCELLED';

interface StatusBadgeConfig {
  label: string;
  className: string;
}

const STATUS_CONFIG: Record<OrderStatus, StatusBadgeConfig> = {
  COMPLETED: {
    label: '주문완료',
    className: 'bg-green-100 text-green-800',
  },
  CANCELLED: {
    label: '취소됨',
    className: 'bg-red-100 text-red-800',
  },
};

function getStatusBadge(status: string): ReactElement {
  const config = STATUS_CONFIG[status as OrderStatus] ?? {
    label: status,
    className: 'bg-gray-100 text-gray-800',
  };

  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}

export function OrderPage(): ReactElement {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);
      const ordersData = await getMyOrders();
      setOrders(ordersData);
    } catch (err) {
      console.warn('Failed to load orders:', err);
      setError('주문 내역을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders();
  }, []);

  const handleRefresh = (): void => {
    void loadOrders();
  };

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-full flex-col items-center justify-center p-4">
        <ErrorMessage message={error} />
        <button
          onClick={handleRefresh}
          className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="flex min-h-full flex-col p-4">
      {/* 헤더 */}
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">주문 내역</h1>
        <button
          onClick={handleRefresh}
          className="rounded-lg bg-white px-3 py-1.5 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50"
        >
          새로고침
        </button>
      </div>

      {/* 주문 목록 */}
      {orders.length === 0 ? (
        <div className="flex flex-1 items-center justify-center rounded-lg bg-gray-50 p-8">
          <div className="text-center">
            <svg
              className="mx-auto mb-3 h-12 w-12 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
              />
            </svg>
            <p className="text-gray-500">아직 주문 내역이 없습니다.</p>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          {orders.map((order) => (
            <div
              key={order.id}
              className="rounded-xl bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h3 className="mb-2 text-lg font-bold text-gray-900">
                    {order.productName}
                  </h3>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold text-red-600">
                      -{order.usedPoint.toLocaleString()}P
                    </span>
                    {getStatusBadge(order.status)}
                  </div>
                  <p className="mt-2 text-sm text-gray-500">
                    {dayjs(order.orderedAt).format('YYYY.MM.DD HH:mm')}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
