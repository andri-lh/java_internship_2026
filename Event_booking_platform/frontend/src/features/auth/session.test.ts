import { afterEach, describe, expect, it } from 'vitest';
import { readSession, removeSession, storeSession } from './session';

function fakeJwt(expiresInSeconds: number): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + expiresInSeconds }));
  return 'header.' + payload + '.signature';
}

afterEach(() => {
  removeSession();
});

describe('session storage', () => {
  it('keeps a normal session in sessionStorage only', () => {
    storeSession({ accessToken: fakeJwt(600), role: 'ATTENDEE' });

    expect(sessionStorage.getItem('event-booking-session')).not.toBeNull();
    expect(localStorage.getItem('event-booking-session')).toBeNull();
    expect(readSession()?.role).toBe('ATTENDEE');
  });

  it('persists a remembered session in localStorage', () => {
    storeSession({ accessToken: fakeJwt(600), role: 'ORGANIZER' }, true);

    expect(localStorage.getItem('event-booking-session')).not.toBeNull();
    expect(sessionStorage.getItem('event-booking-session')).toBeNull();
    expect(readSession()?.role).toBe('ORGANIZER');
  });

  it('discards an expired token instead of restoring the session', () => {
    storeSession({ accessToken: fakeJwt(-60), role: 'ADMIN' }, true);

    expect(readSession()).toBeNull();
  });

  it('removeSession clears both storages', () => {
    storeSession({ accessToken: fakeJwt(600), role: 'ADMIN' }, true);
    removeSession();

    expect(readSession()).toBeNull();
  });
});
