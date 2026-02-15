import { useQuery } from '@tanstack/react-query';
import { getMyOrders } from '../api/order-api';
import type { OrderResponse } from '@/common/types/api';

/** 쿼리 키 상수 — 캐시 무효화 시 재사용 */
export const ORDER_QUERY_KEYS = {
  list: ['order', 'list'] as const,
};

/**
 * 내 주문 내역 조회 훅
 * 로그인한 사용자의 전체 주문 목록을 가져온다.
 */
export function useMyOrders(): ReturnType<typeof useQuery<OrderResponse[]>> {
  return useQuery<OrderResponse[]>({
    queryKey: ORDER_QUERY_KEYS.list,
    queryFn: getMyOrders,
  });
}
