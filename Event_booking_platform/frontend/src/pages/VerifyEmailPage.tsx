import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { verifyEmail } from '../features/auth/authApi';
import { ApiError } from '../services/apiClient';

export function VerifyEmailPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const started = useRef(false);
  const [state, setState] = useState<'working' | 'done' | 'failed'>(token ? 'working' : 'failed');
  const [message, setMessage] = useState(token ? '' : 'This verification link is incomplete.');

  useEffect(() => {
    if (!token || started.current) return;
    started.current = true;
    verifyEmail(token)
      .then(() => setState('done'))
      .catch(failure => {
        setMessage(failure instanceof ApiError && failure.status < 500 ? failure.message : 'We could not verify your email right now. Please try again.');
        setState('failed');
      });
  }, [token]);

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Email verification</span>
        {state === 'working' && <><h1>Verifying…</h1><p>Please wait a moment.</p></>}
        {state === 'done' && (
          <>
            <h1>Email verified</h1>
            <p>Your account is active. You can sign in now.</p>
            <Link className="button button-primary auth-submit" to="/login">Sign in</Link>
          </>
        )}
        {state === 'failed' && (
          <>
            <h1>Link not valid</h1>
            <p className="form-error" role="alert">{message}</p>
            <p>Sign in to request a new verification email.</p>
            <Link className="button button-primary auth-submit" to="/login">Go to sign in</Link>
          </>
        )}
      </div>
    </section>
  );
}
