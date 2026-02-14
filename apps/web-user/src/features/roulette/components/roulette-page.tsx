import { type ReactElement, useEffect, useState } from 'react';
import { RouletteWheel } from './roulette-wheel';
import {
  getRouletteStatus,
  spinRoulette,
  getTodayBudget,
} from '../api/roulette-api';
import type {
  RouletteStatusResponse,
  RouletteResultResponse,
  BudgetResponse,
} from '@/common/types/api';

type SpinState = 'idle' | 'spinning' | 'result';

export function RoulettePage(): ReactElement {
  const [status, setStatus] = useState<RouletteStatusResponse | null>(null);
  const [budget, setBudget] = useState<BudgetResponse | null>(null);
  const [result, setResult] = useState<RouletteResultResponse | null>(null);
  const [spinState, setSpinState] = useState<SpinState>('idle');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 초기 데이터 로드
  useEffect(() => {
    async function loadInitialData(): Promise<void> {
      try {
        setIsLoading(true);
        setError(null);
        const [statusData, budgetData] = await Promise.all([
          getRouletteStatus(),
          getTodayBudget(),
        ]);
        setStatus(statusData);
        setBudget(budgetData);
      } catch (err) {
        console.warn('Failed to load roulette data:', err);
        setError('데이터를 불러오는 중 오류가 발생했습니다.');
      } finally {
        setIsLoading(false);
      }
    }

    void loadInitialData();
  }, []);

  // 룰렛 돌리기 핸들러
  const handleSpin = async (): Promise<void> => {
    if (!status?.canPlay || spinState !== 'idle') return;

    try {
      setError(null);
      setSpinState('spinning');
      const spinResult = await spinRoulette();
      setResult(spinResult);
      // 스핀 애니메이션은 RouletteWheel 컴포넌트에서 처리
    } catch (err) {
      console.warn('Failed to spin roulette:', err);
      setError('룰렛을 돌리는 중 오류가 발생했습니다.');
      setSpinState('idle');
    }
  };

  // 스핀 완료 핸들러
  const handleSpinComplete = (): void => {
    setSpinState('result');
    // 상태 업데이트
    if (status) {
      setStatus({ ...status, hasPlayedToday: true, canPlay: false });
    }
  };

  // 결과 확인 후 초기화
  const handleResultConfirm = (): void => {
    setSpinState('idle');
  };

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="text-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
          <p className="mt-2 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error && !status) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="rounded-lg bg-red-50 p-4 text-center">
          <p className="text-red-800">{error}</p>
        </div>
      </div>
    );
  }

  const budgetPercentage = budget
    ? Math.round((budget.usedBudget / budget.totalBudget) * 100)
    : 0;

  return (
    <div className="flex min-h-full flex-col p-4">
      {/* 헤더 카드 */}
      <div className="mb-6 overflow-hidden rounded-lg bg-gradient-to-r from-indigo-600 to-purple-600 p-6 text-white shadow-lg">
        <h2 className="mb-2 text-xl font-bold">매일 룰렛 이벤트 🎰</h2>
        <p className="text-sm text-indigo-100">
          하루 한 번 룰렛을 돌려 포인트를 받아가세요!
        </p>
      </div>

      {/* 예산 정보 */}
      {budget && (
        <div className="mb-6 rounded-lg bg-white p-4 shadow">
          <div className="mb-2 flex items-center justify-between text-sm">
            <span className="font-medium text-gray-700">오늘의 남은 예산</span>
            <span className="font-bold text-indigo-600">
              {budget.remainingBudget.toLocaleString()} P
            </span>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-gray-200">
            <div
              className="h-full bg-gradient-to-r from-indigo-500 to-purple-500 transition-all duration-300"
              style={{ width: `${budgetPercentage}%` }}
            ></div>
          </div>
          <p className="mt-1 text-xs text-gray-500">
            전체 예산: {budget.totalBudget.toLocaleString()} P (사용:{' '}
            {budgetPercentage}%)
          </p>
        </div>
      )}

      {/* 룰렛 섹션 */}
      <div className="mb-6 flex flex-1 flex-col items-center justify-center">
        <RouletteWheel
          isSpinning={spinState === 'spinning'}
          result={result?.point ?? null}
          onSpinComplete={handleSpinComplete}
        />

        {/* 버튼 */}
        <div className="mt-8 w-full max-w-xs">
          {status?.hasPlayedToday && spinState === 'idle' ? (
            <div className="rounded-lg bg-gray-100 p-4 text-center">
              <p className="font-medium text-gray-700">오늘은 이미 참여했습니다 ✅</p>
              <p className="mt-1 text-sm text-gray-500">내일 다시 도전하세요!</p>
            </div>
          ) : (
            <button
              onClick={handleSpin}
              disabled={!status?.canPlay || spinState !== 'idle'}
              className="w-full rounded-lg bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-3 font-bold text-white shadow-lg transition-all hover:from-indigo-700 hover:to-purple-700 disabled:cursor-not-allowed disabled:from-gray-400 disabled:to-gray-500"
            >
              {spinState === 'spinning' ? '돌리는 중...' : '룰렛 돌리기 🎲'}
            </button>
          )}

          {!status?.canPlay && !status?.hasPlayedToday && (
            <p className="mt-2 text-center text-sm text-red-600">
              예산이 부족하여 참여할 수 없습니다.
            </p>
          )}

          {error && (
            <div className="mt-4 rounded-lg bg-red-50 p-3 text-center text-sm text-red-800">
              {error}
            </div>
          )}
        </div>
      </div>

      {/* 결과 모달 */}
      {spinState === 'result' && result && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="w-full max-w-sm rounded-lg bg-white p-6 text-center shadow-xl">
            <div className="mb-4 text-6xl">🎉</div>
            <h3 className="mb-2 text-2xl font-bold text-gray-900">축하합니다!</h3>
            <p className="mb-4 text-lg text-gray-600">
              <span className="text-3xl font-bold text-indigo-600">
                {result.point.toLocaleString()}
              </span>{' '}
              포인트를 획득했습니다!
            </p>
            <button
              onClick={handleResultConfirm}
              className="w-full rounded-lg bg-indigo-600 px-6 py-3 font-bold text-white hover:bg-indigo-700"
            >
              확인
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
