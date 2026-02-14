import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

export interface BudgetResponse {
  date: string;
  totalBudget: number;
  usedBudget: number;
  remainingBudget: number;
}

export interface RouletteHistory {
  id: number;
  memberId: number;
  point: number;
  isCancelled: boolean;
  playedAt: string;
  createdAt: string;
}

export async function getTodayBudget(): Promise<BudgetResponse> {
  const response = await client.get<ApiResponse<BudgetResponse>>('/api/budget/today');
  return response.data.data!;
}

export async function setTodayBudget(totalBudget: number): Promise<BudgetResponse> {
  const response = await client.put<ApiResponse<BudgetResponse>>('/api/admin/budget/today', { totalBudget });
  return response.data.data!;
}

export async function cancelRoulette(historyId: number): Promise<void> {
  await client.post(`/api/admin/roulette/${historyId}/cancel`);
}
