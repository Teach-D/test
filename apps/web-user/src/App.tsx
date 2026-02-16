import { type ReactElement } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Router } from './router';

/**
 * TanStack Query 전역 클라이언트 설정
 * - staleTime: 5분 동안 캐시 데이터를 신선(fresh)한 상태로 유지
 * - retry: 실패 시 1회만 재시도 (기본값 3회에서 줄임)
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      retry: 1,
    },
  },
});

function App(): ReactElement {
  // 인증 상태는 auth-store.ts에서 스토어 생성 시점에 localStorage로부터 즉시 복원
  return (
    <QueryClientProvider client={queryClient}>
      <Router />
    </QueryClientProvider>
  );
}

export default App;
