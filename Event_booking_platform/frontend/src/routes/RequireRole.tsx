import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';
import type { Role } from '../features/auth/session';

export function RequireRole({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { session } = useAuth();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!roles.includes(session.role)) {
    return <Navigate to="/forbidden" replace />;
  }

  return children;
}
