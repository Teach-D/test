import { type ReactElement } from 'react';
import dayjs from 'dayjs';
import { useMyOrders } from '../hooks/use-order';
import { useQueryClient } from '@tanstack/react-query';
import { ORDER_QUERY_KEYS } from '../hooks/use-order';
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
  const queryClient = useQueryClient();

  // 주문 내역 조회 — TanStack Query가 캐싱과 로딩 상태를 관리
  const ordersQuery = useMyOrders();

  // 새로고침 버튼 클릭 시 캐시를 무효화하여 최신 데이터 재조회
  const handleRefresh = (): void => {
    void queryClient.invalidateQueries({ queryKey: ORDER_QUERY_KEYS.list });
  };

  // 로딩 상태 처리
  if (ordersQuery.isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  // 에러 상태 처리
  if (ordersQuery.isError) {
    return (
      <div className="flex h-full flex-col items-center justify-center p-4">
        <ErrorMessage message="주문 내역을 불러오는 중 오류가 발생했습니다." />
        <button
          onClick={handleRefresh}
          className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  const orders = (ordersQuery.data as OrderResponse[]) ?? [];

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
