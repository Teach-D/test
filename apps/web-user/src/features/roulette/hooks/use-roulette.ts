import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getRouletteStatus, getTodayBudget, spinRoulette } from '../api/roulette-api';
import type {
  RouletteStatusResponse,
  RouletteResultResponse,
  BudgetResponse,
} from '@/common/types/api';

/** 쿼리 키 상수 — 캐시 무효화 시 재사용 */
export const ROULETTE_QUERY_KEYS = {
  status: ['roulette', 'status'] as const,
  budget: ['roulette', 'budget'] as const,
};

/**
 * 룰렛 상태 조회 훅
 * 오늘 참여 여부와 참여 가능 여부를 가져온다.
 */
export function useRouletteStatus(): ReturnType<typeof useQuery<RouletteStatusResponse>> {
  return useQuery<RouletteStatusResponse>({
    queryKey: ROULETTE_QUERY_KEYS.status,
    queryFn: getRouletteStatus,
  });
}

/**
 * 오늘의 예산 조회 훅
 * 총 예산, 사용 예산, 남은 예산을 가져온다.
 */
export function useTodayBudget(): ReturnType<typeof useQuery<BudgetResponse>> {
  return useQuery<BudgetResponse>({
    queryKey: ROULETTE_QUERY_KEYS.budget,
    queryFn: getTodayBudget,
  });
}

/**
 * 룰렛 돌리기 뮤테이션 훅
 * 성공 또는 특정 에러 발생 시 룰렛 상태와 예산 캐시를 무효화하여 최신 데이터를 가져온다.
 */
export function useSpinRoulette(): ReturnType<
  typeof useMutation<RouletteResultResponse, Error, void>
> {
  const queryClient = useQueryClient();

  return useMutation<RouletteResultResponse, Error, void>({
    mutationFn: spinRoulette,
    onSuccess: () => {
      // 성공 시 룰렛 상태와 예산 캐시를 무효화하여 재조회
      void queryClient.invalidateQueries({ queryKey: ROULETTE_QUERY_KEYS.status });
      void queryClient.invalidateQueries({ queryKey: ROULETTE_QUERY_KEYS.budget });
    },
    onError: () => {
      // 에러 발생 시에도 최신 상태를 가져와서 UI에 반영 (이미 참여한 경우 등)
      void queryClient.invalidateQueries({ queryKey: ROULETTE_QUERY_KEYS.status });
      void queryClient.invalidateQueries({ queryKey: ROULETTE_QUERY_KEYS.budget });
    },
  });
}
