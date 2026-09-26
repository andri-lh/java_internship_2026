import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { setUnauthorizedHandler } from '../../services/apiClient';
import {
  readSession,
  removeSession,
  storeSession,
  type AuthSession,
} from './session';

interface AuthContextValue {
  session: AuthSession | null;
  signIn: (session: AuthSession, remember?: boolean) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(readSession);

  const value = useMemo<AuthContextValue>(() => ({
    session,
    signIn(nextSession, remember = false) {
      storeSession(nextSession, remember);
      setSession(nextSession);
    },
    signOut() {
      removeSession();
      setSession(null);
    },
  }), [session]);

  useEffect(() => {
    setUnauthorizedHandler(value.signOut);
    return () => setUnauthorizedHandler(null);
  }, [value]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider.');
  return context;
}
