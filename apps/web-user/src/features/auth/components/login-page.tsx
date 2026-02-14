import { useState, type ReactElement, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth-api';
import { useAuthStore } from '@/common/stores/auth-store';
import { LoadingSpinner } from '@/common/components/loading-spinner';
import { ErrorMessage } from '@/common/components/error-message';

export function LoginPage(): ReactElement {
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = useAuthStore((state) => state.login);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent): Promise<void> => {
    e.preventDefault();
    setError(null);

    // 닉네임 유효성 검사
    if (nickname.length < 2 || nickname.length > 50) {
      setError('닉네임은 2-50자 사이여야 합니다.');
      return;
    }

    try {
      setIsLoading(true);
      const response = await authApi.login({ nickname });
      login(response);
      navigate('/', { replace: true });
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message || '로그인에 실패했습니다.');
      } else {
        setError('로그인에 실패했습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md">
        <div className="rounded-lg bg-white p-8 shadow-lg">
          {/* App Title */}
          <div className="mb-8 text-center">
            <h1 className="mb-2 text-3xl font-bold text-gray-900">
              포인트 룰렛
            </h1>
            <p className="text-sm text-gray-600">
              매일 룰렛을 돌려 포인트를 획득하세요
            </p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label
                htmlFor="nickname"
                className="block text-sm font-medium text-gray-700"
              >
                닉네임
              </label>
              <input
                id="nickname"
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500"
                placeholder="2-50자 입력"
                required
                disabled={isLoading}
              />
            </div>

            {error && <ErrorMessage message={error} />}

            <button
              type="submit"
              disabled={isLoading}
              className="flex w-full justify-center rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isLoading ? <LoadingSpinner /> : '로그인'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
