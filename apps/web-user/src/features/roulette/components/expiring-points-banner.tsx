import { type ReactElement } from 'react';
import { Link } from 'react-router-dom';
import { usePointBalance } from '@/features/point/hooks/use-point';

/**
 * 7일 내 만료 예정 포인트 알림 배너
 * 만료 예정 포인트가 없으면 아무것도 렌더링하지 않는다.
 */
export function ExpiringPointsBanner(): ReactElement | null {
  // usePointBalance 훅 — TanStack Query 캐시를 공유하므로 추가 API 호출 없이 재사용된다
  const { data: balance } = usePointBalance();

  // 만료 예정 포인트가 없으면 배너를 표시하지 않음
  if (!balance || balance.expiringSoonBalance <= 0) {
    return null;
  }

  return (
    <div className="mb-4 rounded-lg border-l-4 border-amber-500 bg-amber-50 p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {/* 경고 아이콘 */}
          <span className="text-lg" aria-hidden="true">
            ⚠️
          </span>
          <p className="text-sm font-medium text-amber-800">
            {/* 만료 예정 포인트 총액 표시 */}
            7일 내 만료 예정:{' '}
            <span className="font-bold">{balance.expiringSoonBalance.toLocaleString()}P</span>
          </p>
        </div>
        {/* 포인트 페이지로 이동하는 링크 */}
        <Link
          to="/points"
          className="shrink-0 rounded-md bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800 transition-colors hover:bg-amber-200"
        >
          포인트 확인하기 →
        </Link>
      </div>
    </div>
  );
}
