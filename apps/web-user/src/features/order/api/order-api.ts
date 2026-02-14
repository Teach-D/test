import client from '@/common/api/client';
import type { OrderRequest, OrderResponse } from '@/common/types/api';

/**
 * 상품 구매
 * @param request 구매할 상품 정보
 * @returns 주문 정보
 */
export async function placeOrder(request: OrderRequest): Promise<OrderResponse> {
  const response = await client.post<OrderResponse>('/api/orders', request);
  return response.data;
}

/**
 * 내 주문 내역 조회
 * @returns 주문 목록
 */
export async function getMyOrders(): Promise<OrderResponse[]> {
  const response = await client.get<OrderResponse[]>('/api/orders');
  return response.data;
}
