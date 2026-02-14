import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

export interface Order {
  id: number;
  productId: number;
  productName: string;
  usedPoint: number;
  status: 'COMPLETED' | 'CANCELLED';
  orderedAt: string;
}

export async function getAllOrders(): Promise<Order[]> {
  const response = await client.get<ApiResponse<Order[]>>('/api/admin/orders');
  return response.data.data!;
}

export async function cancelOrder(orderId: number): Promise<Order> {
  const response = await client.post<ApiResponse<Order>>(`/api/admin/orders/${orderId}/cancel`);
  return response.data.data!;
}
