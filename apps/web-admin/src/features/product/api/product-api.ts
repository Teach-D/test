import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  isActive: boolean;
}

export interface ProductCreateRequest {
  name: string;
  description?: string;
  price: number;
  stock: number;
}

export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  stock?: number;
  isActive?: boolean;
}

export async function getAllProducts(): Promise<Product[]> {
  const response = await client.get<ApiResponse<Product[]>>('/api/admin/products');
  return response.data.data!;
}

export async function createProduct(data: ProductCreateRequest): Promise<Product> {
  const response = await client.post<ApiResponse<Product>>('/api/admin/products', data);
  return response.data.data!;
}

export async function updateProduct(id: number, data: ProductUpdateRequest): Promise<Product> {
  const response = await client.patch<ApiResponse<Product>>(`/api/admin/products/${id}`, data);
  return response.data.data!;
}

export async function deleteProduct(id: number): Promise<void> {
  await client.delete(`/api/admin/products/${id}`);
}
