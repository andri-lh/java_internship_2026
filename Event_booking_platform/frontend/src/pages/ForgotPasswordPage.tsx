import { useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { requestPasswordReset } from '../features/auth/authApi';
import { ApiError } from '../services/apiClient';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = email.trim();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      setError('Enter a valid email address.');
      return;
    }

    setError('');
    setSubmitting(true);
    try {
      await requestPasswordReset(value);
      setSent(true);
    } catch (failure) {
      setError(failure instanceof ApiError && failure.status < 500 ? failure.message : 'We could not send the reset link right now. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Account recovery</span>
        <h1>Forgot your password?</h1>
        {sent ? (
          <>
            <p className="form-success" role="status">If an account exists for {email.trim()}, we have sent a link to reset the password. It is valid for 30 minutes.</p>
            <p>Did not get it? Check your spam folder, or wait a minute and try again.</p>
            <div className="form-actions">
              <button className="button button-outline" type="button" onClick={() => setSent(false)}>Try another email</button>
              <Link className="button button-primary" to="/login">Back to sign in</Link>
            </div>
          </>
        ) : (
          <>
            <p>Enter the email address of your account and we will send you a link to choose a new password.</p>
            <form onSubmit={submit} noValidate>
              <label className="field"><span>Email</span><input type="email" autoComplete="email" maxLength={254} value={email} onChange={event => setEmail(event.target.value)} aria-invalid={!!error} /></label>
              {error && <p className="form-error" role="alert">{error}</p>}
              <button className="button button-primary auth-submit" type="submit" disabled={submitting || !email.trim()}>{submitting ? 'Sending…' : 'Send reset link'}</button>
            </form>
            <p className="auth-switch">Remembered it? <Link to="/login">Sign in</Link></p>
          </>
        )}
      </div>
    </section>
  );
}
