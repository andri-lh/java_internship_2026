import { useState, type FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';
import { register } from '../features/auth/authApi';
import { PasswordField } from '../components/PasswordField';
import { isStrongPassword } from '../features/auth/passwordRules';
import { ApiError } from '../services/apiClient';

type FieldErrors = Partial<Record<'username' | 'email' | 'password', string>>;

export function RegisterPage() {
  const { session } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (session) return <Navigate to="/events" replace />;

  function update(key: keyof typeof form, value: string) {
    setForm(current => ({ ...current, [key]: value }));
  }

  function validate(): FieldErrors {
    const errors: FieldErrors = {};
    const username = form.username.trim();
    if (username.length < 3 || username.length > 50) errors.username = 'Username must be 3 to 50 characters.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = 'Enter a valid email address.';
    if (!isStrongPassword(form.password)) errors.password = 'Password does not meet all the requirements below.';
    return errors;
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const errors = validate();
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      await register({ username: form.username.trim(), email: form.email.trim(), password: form.password });
      navigate('/login', { replace: true, state: { registered: true } });
    } catch (failure) {
      if (failure instanceof ApiError && failure.status < 500) {
        const hasFieldErrors = Object.keys(failure.validationErrors).length > 0;
        setFieldErrors(failure.validationErrors as FieldErrors);
        setError(hasFieldErrors ? '' : failure.message);
      } else {
        setError('We could not create your account right now. Please try again.');
      }
      setSubmitting(false);
    }
  }

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Join EventBooking</span>
        <h1>Create your account</h1>
        <p>Register as an attendee to reserve seats and review events.</p>
        <form onSubmit={submit} noValidate>
          <label className="field"><span>Username</span><input type="text" autoComplete="username" maxLength={50} value={form.username} onChange={event => update('username', event.target.value)} aria-invalid={!!fieldErrors.username} />{fieldErrors.username && <em className="field-error">{fieldErrors.username}</em>}</label>
          <label className="field"><span>Email</span><input type="email" autoComplete="email" maxLength={254} value={form.email} onChange={event => update('email', event.target.value)} aria-invalid={!!fieldErrors.email} />{fieldErrors.email && <em className="field-error">{fieldErrors.email}</em>}</label>
          <PasswordField value={form.password} onChange={value => update('password', value)} autoComplete="new-password" error={fieldErrors.password} showCriteria />
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}</button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
      </div>
    </section>
  );
}
