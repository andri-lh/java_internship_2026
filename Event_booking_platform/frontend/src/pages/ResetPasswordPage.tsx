import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { PasswordField } from '../components/PasswordField';
import { resetPassword } from '../features/auth/authApi';
import { isStrongPassword } from '../features/auth/passwordRules';
import { ApiError } from '../services/apiClient';

export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<{ password?: string; confirm?: string }>({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!token) {
    return (
      <section className="container auth-page">
        <div className="auth-card">
          <span className="eyebrow">Account recovery</span>
          <h1>Link not valid</h1>
          <p>This password reset link is incomplete. Request a new one to continue.</p>
          <Link className="button button-primary auth-submit" to="/forgot-password">Request a new link</Link>
        </div>
      </section>
    );
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const found: typeof errors = {};
    if (!isStrongPassword(password)) found.password = 'Password does not meet all the requirements below.';
    if (confirm !== password) found.confirm = 'The passwords do not match.';
    setErrors(found);
    setError('');
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      await resetPassword(token, password);
      navigate('/login', { replace: true, state: { passwordReset: true } });
    } catch (failure) {
      setError(failure instanceof ApiError && failure.status < 500 ? failure.message : 'We could not update your password right now. Please try again.');
      setSubmitting(false);
    }
  }

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Account recovery</span>
        <h1>Choose a new password</h1>
        <p>Pick a strong password you have not used elsewhere.</p>
        <form onSubmit={submit} noValidate>
          <PasswordField label="New password" value={password} onChange={setPassword} autoComplete="new-password" error={errors.password} showCriteria />
          <PasswordField label="Confirm new password" value={confirm} onChange={setConfirm} autoComplete="new-password" error={errors.confirm} />
          {error && <p className="form-error" role="alert">{error} <Link to="/forgot-password">Request a new link</Link></p>}
          <button className="button button-primary auth-submit" type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Update password'}</button>
        </form>
      </div>
    </section>
  );
}
