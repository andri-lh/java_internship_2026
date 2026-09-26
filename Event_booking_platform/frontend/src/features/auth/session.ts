export type Role = 'ADMIN' | 'ORGANIZER' | 'ATTENDEE';

export interface AuthSession {
  accessToken: string;
  role: Role;
}

const STORAGE_KEY = 'event-booking-session';

function isExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
    return typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now();
  } catch {
    return false;
  }
}

function parse(stored: string | null): AuthSession | null {
  if (!stored) return null;

  const value: unknown = JSON.parse(stored);
  if (typeof value !== 'object' || value === null) return null;

  const session = value as Partial<AuthSession>;
  const validRole = session.role === 'ADMIN'
    || session.role === 'ORGANIZER'
    || session.role === 'ATTENDEE';

  return typeof session.accessToken === 'string' && validRole && !isExpired(session.accessToken)
    ? { accessToken: session.accessToken, role: session.role as Role }
    : null;
}

export function readSession(): AuthSession | null {
  try {
    return parse(sessionStorage.getItem(STORAGE_KEY)) ?? parse(localStorage.getItem(STORAGE_KEY));
  } catch {
    return null;
  }
}

// "Remember me" keeps the session across browser restarts; otherwise it ends with the tab.
export function storeSession(session: AuthSession, remember = false): void {
  removeSession();
  (remember ? localStorage : sessionStorage).setItem(STORAGE_KEY, JSON.stringify(session));
}

export function removeSession(): void {
  sessionStorage.removeItem(STORAGE_KEY);
  localStorage.removeItem(STORAGE_KEY);
}
