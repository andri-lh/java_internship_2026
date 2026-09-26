import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';
import { login, resendVerification } from '../features/auth/authApi';
import { roleHome } from '../features/auth/roleHome';
import { PasswordField } from '../components/PasswordField';
import { ApiError } from '../services/apiClient';

interface LocationState {
  from?: { pathname: string; search?: string };
  registered?: boolean;
  passwordReset?: boolean;
}

export function LoginPage() {
  const { session, signIn } = useAuth();
  const state = (useLocation().state ?? {}) as LocationState;
  const returnTo = state.from ? state.from.pathname + (state.from.search ?? '') : null;

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [unverified, setUnverified] = useState(false);
  const [resendEmail, setResendEmail] = useState('');
  const [resendNotice, setResendNotice] = useState('');

  if (session) return <Navigate to={returnTo ?? (session.role === 'ATTENDEE' ? '/events' : roleHome(session.role).path)} replace />;

  async function resend() {
    try {
      await resendVerification(resendEmail.trim());
      setResendNotice('If that account still needs verification, a new link is on its way.');
    } catch {
      setResendNotice('We could not send the email right now. Please try again.');
    }
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setUnverified(false);
    setResendNotice('');
    setSubmitting(true);
    try {
      const response = await login({ username: username.trim(), password });
      signIn({ accessToken: response.accessToken, role: response.role }, remember);
    } catch (failure) {
      setUnverified(failure instanceof ApiError && failure.status === 403);
      setError(failure instanceof ApiError && failure.status < 500
        ? failure.message
        : 'We could not sign you in right now. Please try again.');
      setSubmitting(false);
    }
  }

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Welcome back</span>
        <h1>Sign in</h1>
        <p>Access your bookings and manage your events.</p>
        {state.registered && <p className="form-success" role="status">Your account was created. If we sent you a verification email, confirm it first, then sign in.</p>}
        {state.passwordReset && <p className="form-success" role="status">Your password was updated. Sign in with the new one.</p>}
        <form onSubmit={submit} noValidate>
          <label className="field"><span>Username</span><input type="text" autoComplete="username" maxLength={50} value={username} onChange={event => setUsername(event.target.value)} /></label>
          <PasswordField value={password} onChange={setPassword} autoComplete="current-password" />
          <Link className="auth-forgot" to="/forgot-password">Forgot your password?</Link>
          <label className="remember-me"><input type="checkbox" checked={remember} onChange={event => setRemember(event.target.checked)} /><span>Remember me on this device</span></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          {unverified && (
            <div className="resend-box">
              <label className="field"><span>Email used to register</span><input type="email" autoComplete="email" value={resendEmail} onChange={event => setResendEmail(event.target.value)} /></label>
              <button className="button button-outline" type="button" disabled={!resendEmail.trim()} onClick={() => void resend()}>Resend verification email</button>
              {resendNotice && <p className="form-success" role="status">{resendNotice}</p>}
            </div>
          )}
          <button className="button button-primary auth-submit" type="submit" disabled={submitting || !username.trim() || !password}>{submitting ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <p className="auth-switch">New here? <Link to="/register">Create an account</Link></p>
      </div>
    </section>
  );
}
