import { useQuery } from '@tanstack/react-query';
import { getPointBalance, getPoints, getExpiringSoonPoints } from '../api/point-api';
import type { PointBalanceResponse, PointResponse } from '@/common/types/api';

/** 쿼리 키 상수 — 캐시 무효화 시 재사용 */
export const POINT_QUERY_KEYS = {
  balance: ['point', 'balance'] as const,
  list: ['point', 'list'] as const,
  expiringSoon: ['point', 'expiring-soon'] as const,
};

/**
 * 포인트 잔액 조회 훅
 * 사용 가능한 총 포인트와 만료 예정 포인트 합계를 가져온다.
 */
export function usePointBalance(): ReturnType<typeof useQuery<PointBalanceResponse>> {
  return useQuery<PointBalanceResponse>({
    queryKey: POINT_QUERY_KEYS.balance,
    queryFn: getPointBalance,
    staleTime: 0, // 페이지 진입 시 항상 최신 잔액 조회 (어드민 취소 반영)
  });
}

/**
 * 포인트 내역 목록 조회 훅
 * 적립된 포인트 전체 내역을 가져온다.
 */
export function usePoints(): ReturnType<typeof useQuery<PointResponse[]>> {
  return useQuery<PointResponse[]>({
    queryKey: POINT_QUERY_KEYS.list,
    queryFn: getPoints,
    staleTime: 0, // 페이지 진입 시 항상 최신 내역 조회
  });
}

/**
 * 만료 예정 포인트 조회 훅
 * 7일 이내 만료되는 포인트 목록을 가져온다.
 */
export function useExpiringSoonPoints(): ReturnType<typeof useQuery<PointResponse[]>> {
  return useQuery<PointResponse[]>({
    queryKey: POINT_QUERY_KEYS.expiringSoon,
    queryFn: getExpiringSoonPoints,
    staleTime: 0, // 페이지 진입 시 항상 최신 만료 예정 조회
  });
}
