import client from '@/common/api/client';
import type {
  PointBalanceResponse,
  PointResponse,
} from '@/common/types/api';

export async function getPointBalance(): Promise<PointBalanceResponse> {
  const response = await client.get<PointBalanceResponse>('/api/points/balance');
  return response.data;
}

export async function getPoints(): Promise<PointResponse[]> {
  const response = await client.get<PointResponse[]>('/api/points');
  return response.data;
}

export async function getExpiringSoonPoints(): Promise<PointResponse[]> {
  const response = await client.get<PointResponse[]>('/api/points/expiring-soon');
  return response.data;
}
