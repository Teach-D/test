import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getProducts } from '../api/product-api';
import { placeOrder } from '@/features/order/api/order-api';
import { POINT_QUERY_KEYS } from '@/features/point/hooks/use-point';
import type { ProductResponse, OrderRequest, OrderResponse } from '@/common/types/api';

/** 쿼리 키 상수 — 캐시 무효화 시 재사용 */
export const PRODUCT_QUERY_KEYS = {
  list: ['product', 'list'] as const,
};

/** 주문 쿼리 키 — order 훅과 공유 */
export const ORDER_QUERY_KEY = ['order', 'list'] as const;

/**
 * 상품 목록 조회 훅
 * 활성화된 상품 전체 목록을 가져온다.
 */
export function useProducts(): ReturnType<typeof useQuery<ProductResponse[]>> {
  return useQuery<ProductResponse[]>({
    queryKey: PRODUCT_QUERY_KEYS.list,
    queryFn: getProducts,
  });
}

/**
 * 상품 구매 뮤테이션 훅
 * 성공 시 포인트 잔액, 상품 목록, 주문 내역 캐시를 무효화한다.
 */
export function usePlaceOrder(): ReturnType<
  typeof useMutation<OrderResponse, Error, OrderRequest>
> {
  const queryClient = useQueryClient();

  return useMutation<OrderResponse, Error, OrderRequest>({
    mutationFn: placeOrder,
    onSuccess: () => {
      // 포인트 잔액 갱신 (구매로 포인트가 차감됨)
      void queryClient.invalidateQueries({ queryKey: POINT_QUERY_KEYS.balance });
      // 상품 목록 갱신 (재고가 변경될 수 있음)
      void queryClient.invalidateQueries({ queryKey: PRODUCT_QUERY_KEYS.list });
      // 주문 내역 갱신
      void queryClient.invalidateQueries({ queryKey: ORDER_QUERY_KEY });
    },
  });
}
