import client from '@/common/api/client';
import type {
  RouletteStatusResponse,
  RouletteResultResponse,
  BudgetResponse,
} from '@/common/types/api';

/**
 * 룰렛 참여 상태 조회
 * @returns 오늘 참여 여부, 남은 예산, 참여 가능 여부
 */
export async function getRouletteStatus(): Promise<RouletteStatusResponse> {
  const response = await client.get<RouletteStatusResponse>('/api/roulette/status');
  return response.data;
}

/**
 * 룰렛 돌리기
 * @returns 획득한 포인트와 참여 시각
 */
export async function spinRoulette(): Promise<RouletteResultResponse> {
  const response = await client.post<RouletteResultResponse>('/api/roulette/spin');
  return response.data;
}

/**
 * 오늘의 예산 조회
 * @returns 총 예산, 사용 예산, 남은 예산
 */
export async function getTodayBudget(): Promise<BudgetResponse> {
  const response = await client.get<BudgetResponse>('/api/budget/today');
  return response.data;
}
