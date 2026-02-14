import { type ReactElement, useEffect, useState } from 'react';
import dayjs from 'dayjs';
import {
  getPointBalance,
  getPoints,
  getExpiringSoonPoints,
} from '../api/point-api';
import type {
  PointBalanceResponse,
  PointResponse,
} from '@/common/types/api';
import { LoadingSpinner } from '@/common/components/loading-spinner';
import { ErrorMessage } from '@/common/components/error-message';

type PointStatus = 'available' | 'expiring-soon' | 'used' | 'expired';

function getPointStatus(point: PointResponse): PointStatus {
  if (point.isExpired) return 'expired';
  if (point.remainingAmount === 0) return 'used';

  const now = dayjs();
  const expiresAt = dayjs(point.expiresAt);
  const daysUntilExpiry = expiresAt.diff(now, 'day');

  if (daysUntilExpiry <= 7) return 'expiring-soon';

  return 'available';
}

function getStatusBadge(status: PointStatus): ReactElement {
  const badgeClasses: Record<PointStatus, string> = {
    expired: 'bg-red-100 text-red-700',
    'expiring-soon': 'bg-orange-100 text-orange-700',
    used: 'bg-gray-100 text-gray-700',
    available: 'bg-green-100 text-green-700',
  };

  const labels: Record<PointStatus, string> = {
    expired: '만료됨',
    'expiring-soon': '만료 임박',
    used: '사용 완료',
    available: '사용 가능',
  };

  return (
    <span
      className={`rounded-full px-2 py-1 text-xs font-medium ${badgeClasses[status]}`}
    >
      {labels[status]}
    </span>
  );
}

export function PointPage(): ReactElement {
  const [balance, setBalance] = useState<PointBalanceResponse | null>(null);
  const [points, setPoints] = useState<PointResponse[]>([]);
  const [expiringSoonPoints, setExpiringSoonPoints] = useState<PointResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadPointData(): Promise<void> {
      try {
        setIsLoading(true);
        setError(null);
        const [balanceData, pointsData, expiringSoonData] = await Promise.all([
          getPointBalance(),
          getPoints(),
          getExpiringSoonPoints(),
        ]);
        setBalance(balanceData);
        setPoints(pointsData);
        setExpiringSoonPoints(expiringSoonData);
      } catch (err) {
        const message = err instanceof Error ? err.message : '포인트 정보를 불러오는 중 오류가 발생했습니다.';
        setError(message);
      } finally {
        setIsLoading(false);
      }
    }

    void loadPointData();
  }, []);

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <ErrorMessage message={error} />
      </div>
    );
  }

  return (
    <div className="flex min-h-full flex-col p-4">
      {/* 잔액 요약 카드 */}
      <div className="mb-6 overflow-hidden rounded-lg bg-gradient-to-r from-indigo-600 to-purple-600 p-6 text-white shadow-lg">
        <p className="mb-2 text-sm text-indigo-100">사용 가능한 포인트</p>
        <p className="text-4xl font-bold">{balance?.totalBalance.toLocaleString() ?? 0}P</p>
      </div>

      {/* 만료 예정 알림 */}
      {balance && balance.expiringSoonBalance > 0 && (
        <div className="mb-6 rounded-lg border-l-4 border-amber-600 bg-amber-50 p-4">
          <div className="flex items-start">
            <span className="text-xl">⚠️</span>
            <div className="ml-3 flex-1">
              <p className="font-medium text-amber-800">
                7일 내 만료 예정 포인트: {balance.expiringSoonBalance.toLocaleString()}P
              </p>
              {expiringSoonPoints.length > 0 && (
                <div className="mt-3 space-y-2">
                  {expiringSoonPoints.map((point) => (
                    <div
                      key={point.id}
                      className="flex items-center justify-between rounded bg-white px-3 py-2 text-sm"
                    >
                      <span className="text-gray-700">
                        {point.remainingAmount.toLocaleString()}P
                      </span>
                      <span className="text-amber-700">
                        {dayjs(point.expiresAt).format('YYYY.MM.DD HH:mm')} 만료
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 포인트 내역 */}
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-bold text-gray-900">포인트 내역</h2>
      </div>

      {points.length === 0 ? (
        <div className="flex flex-1 items-center justify-center rounded-lg bg-gray-50 p-8">
          <p className="text-gray-500">아직 획득한 포인트가 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {points.map((point) => {
            const status = getPointStatus(point);
            const isUsed = point.remainingAmount !== point.amount;

            return (
              <div
                key={point.id}
                className="rounded-lg bg-white p-4 shadow transition-shadow hover:shadow-md"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-lg font-bold text-indigo-600">
                        +{point.amount.toLocaleString()}P
                      </span>
                      {getStatusBadge(status)}
                    </div>
                    {isUsed && (
                      <p className="mt-1 text-sm text-gray-600">
                        잔여 {point.remainingAmount.toLocaleString()}P
                      </p>
                    )}
                    <div className="mt-2 space-y-1 text-xs text-gray-500">
                      <p>적립: {dayjs(point.earnedAt).format('YYYY.MM.DD HH:mm')}</p>
                      <p>만료: {dayjs(point.expiresAt).format('YYYY.MM.DD HH:mm')}</p>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
