import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../stores/auth-store';

interface PrivateRouteProps {
  children: React.ReactNode;
}

/** 인증 + ADMIN 권한이 있어야 접근 가능한 라우트 */
export function PrivateRoute({ children }: PrivateRouteProps): React.ReactElement {
  const { isLoggedIn, role } = useAuthStore();

  if (!isLoggedIn || role !== 'ADMIN') {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
