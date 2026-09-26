import { useState, type FormEvent } from 'react';
import { PasswordField } from '../components/PasswordField';
import { changePassword } from '../features/auth/authApi';
import { isStrongPassword } from '../features/auth/passwordRules';
import { ApiError } from '../services/apiClient';

export function ChangePasswordPage() {
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<{ current?: string; next?: string; confirm?: string }>({});
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const found: typeof errors = {};
    if (!current) found.current = 'Enter your current password.';
    if (!isStrongPassword(next)) found.next = 'Password does not meet all the requirements below.';
    else if (next === current) found.next = 'Choose a password different from the current one.';
    if (confirm !== next) found.confirm = 'The passwords do not match.';
    setErrors(found);
    setMessage(null);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      await changePassword(current, next);
      setMessage({ error: false, text: 'Your password was changed.' });
      setCurrent('');
      setNext('');
      setConfirm('');
    } catch (failure) {
      setMessage({ error: true, text: failure instanceof ApiError && failure.status < 500 ? failure.message : 'We could not change your password right now. Please try again.' });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="container auth-page">
      <div className="auth-card">
        <span className="eyebrow">Account</span>
        <h1>Change password</h1>
        <p>Enter your current password, then choose a new one.</p>
        <form onSubmit={submit} noValidate>
          <PasswordField label="Current password" value={current} onChange={setCurrent} autoComplete="current-password" error={errors.current} />
          <PasswordField label="New password" value={next} onChange={setNext} autoComplete="new-password" error={errors.next} showCriteria />
          <PasswordField label="Confirm new password" value={confirm} onChange={setConfirm} autoComplete="new-password" error={errors.confirm} />
          {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Change password'}</button>
        </form>
      </div>
    </section>
  );
}
