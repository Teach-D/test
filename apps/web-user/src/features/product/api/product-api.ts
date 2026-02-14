import client from '@/common/api/client';
import type { ProductResponse } from '@/common/types/api';

/**
 * 활성화된 상품 목록 조회
 * @returns 상품 목록
 */
export async function getProducts(): Promise<ProductResponse[]> {
  const response = await client.get<ProductResponse[]>('/api/products');
  return response.data;
}
