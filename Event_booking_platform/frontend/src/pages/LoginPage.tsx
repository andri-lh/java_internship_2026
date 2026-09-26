import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';
import { login } from '../features/auth/authApi';
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
  const navigate = useNavigate();
  const state = (useLocation().state ?? {}) as LocationState;
  const destination = state.from ? state.from.pathname + (state.from.search ?? '') : '/events';

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (session) return <Navigate to={destination} replace />;

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const response = await login({ username: username.trim(), password });
      signIn({ accessToken: response.accessToken, role: response.role }, remember);
      navigate(state.from || response.role === 'ATTENDEE' ? destination : roleHome(response.role).path, { replace: true });
    } catch (failure) {
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
        {state.registered && <p className="form-success" role="status">Your account is ready. Sign in to continue.</p>}
        {state.passwordReset && <p className="form-success" role="status">Your password was updated. Sign in with the new one.</p>}
        <form onSubmit={submit} noValidate>
          <label className="field"><span>Username</span><input type="text" autoComplete="username" maxLength={50} value={username} onChange={event => setUsername(event.target.value)} /></label>
          <PasswordField value={password} onChange={setPassword} autoComplete="current-password" />
          <Link className="auth-forgot" to="/forgot-password">Forgot your password?</Link>
          <label className="remember-me"><input type="checkbox" checked={remember} onChange={event => setRemember(event.target.checked)} /><span>Remember me on this device</span></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={submitting || !username.trim() || !password}>{submitting ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <p className="auth-switch">New here? <Link to="/register">Create an account</Link></p>
      </div>
    </section>
  );
}
