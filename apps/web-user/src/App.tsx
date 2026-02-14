import { useEffect, type ReactElement } from 'react';
import { Router } from './router';
import { useAuthStore } from './common/stores/auth-store';

function App(): ReactElement {
  const initialize = useAuthStore((state) => state.initialize);

  useEffect(() => {
    // 앱 시작 시 localStorage에서 인증 정보 복원
    initialize();
  }, [initialize]);

  return <Router />;
}

export default App;
