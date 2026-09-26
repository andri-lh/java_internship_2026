import { useEffect, useState, type FormEvent } from 'react';
import { PasswordField } from '../components/PasswordField';
import { isStrongPassword } from '../features/auth/passwordRules';
import { Pager } from '../components/Pager';
import { createUser, listUsers, setUserActive, updateUser, type AdminUser } from '../features/admin/adminApi';
import { errorText } from '../features/admin/errors';
import type { Role } from '../features/auth/session';
import type { PageResponse } from '../features/events/types';
import { ApiError } from '../services/apiClient';

const roles: Role[] = ['ATTENDEE', 'ORGANIZER', 'ADMIN'];
const emptyForm = { username: '', email: '', password: '', role: 'ATTENDEE' as Role };

export function AdminUsersPage() {
  const [pageNumber, setPageNumber] = useState(0);
  const [page, setPage] = useState<PageResponse<AdminUser> | null>(null);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    listUsers(pageNumber, controller.signal)
      .then(result => {
        setPage(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [pageNumber, reload]);

  function reset() {
    setEditingId(null);
    setForm(emptyForm);
    setFieldErrors({});
  }

  function edit(user: AdminUser) {
    setEditingId(user.id);
    setForm({ username: user.username, email: user.email, password: '', role: user.role });
    setFieldErrors({});
    setMessage(null);
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const found: Record<string, string> = {};
    const username = form.username.trim();
    if (username.length < 3 || username.length > 50) found.username = 'Username must be 3 to 50 characters.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) found.email = 'Enter a valid email address.';
    if (editingId === null && !isStrongPassword(form.password)) found.password = 'Password does not meet all the requirements below.';
    setFieldErrors(found);
    if (Object.keys(found).length > 0) return;

    setBusy(true);
    setMessage(null);
    try {
      const details = { username, email: form.email.trim(), role: form.role };
      if (editingId === null) await createUser({ ...details, password: form.password });
      else await updateUser(editingId, details);
      setMessage({ error: false, text: editingId === null ? 'Account created.' : 'Account updated.' });
      reset();
      setReload(value => value + 1);
    } catch (failure) {
      if (failure instanceof ApiError && Object.keys(failure.validationErrors).length > 0) setFieldErrors(failure.validationErrors);
      else setMessage({ error: true, text: errorText(failure, 'The account could not be saved. Please try again.') });
    } finally {
      setBusy(false);
    }
  }

  async function toggleActive(user: AdminUser) {
    setMessage(null);
    try {
      await setUserActive(user.id, !user.active);
      setMessage({ error: false, text: user.username + (user.active ? ' was deactivated.' : ' was activated.') });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: errorText(failure, 'The account status could not be changed. Please try again.') });
    }
  }

  const set = (key: keyof typeof emptyForm, value: string) => setForm(current => ({ ...current, [key]: value }));
  const fieldError = (key: string) => fieldErrors[key] && <em className="field-error">{fieldErrors[key]}</em>;

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Administration</span><h1>Users</h1><p>Create accounts, change roles, and control who can sign in.</p></div>
      <div className="admin-layout">
        <form className="filter-panel admin-form" onSubmit={submit} noValidate>
          <div className="filter-panel-header"><h2>{editingId === null ? 'New account' : 'Edit account'}</h2>{editingId !== null && <button className="link-button" type="button" onClick={reset}>Cancel edit</button>}</div>
          <label className="field"><span>Username</span><input type="text" maxLength={50} value={form.username} onChange={change => set('username', change.target.value)} aria-invalid={!!fieldErrors.username} />{fieldError('username')}</label>
          <label className="field"><span>Email</span><input type="email" maxLength={254} value={form.email} onChange={change => set('email', change.target.value)} aria-invalid={!!fieldErrors.email} />{fieldError('email')}</label>
          {editingId === null && <PasswordField value={form.password} onChange={value => set('password', value)} autoComplete="new-password" error={fieldErrors.password} showCriteria />}
          <label className="field"><span>Role</span><select value={form.role} onChange={change => set('role', change.target.value)}>{roles.map(role => <option key={role} value={role}>{role.toLowerCase()}</option>)}</select></label>
          <button className="button button-primary filter-submit" type="submit" disabled={busy}>{busy ? 'Saving…' : editingId === null ? 'Create account' : 'Save changes'}</button>
        </form>

        <section>
          {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
          {state === 'loading' && <div className="status-panel">Loading users…</div>}
          {state === 'error' && <div className="status-panel"><strong>We could not load the users.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
          {state === 'ready' && page && (
            <>
              <ul className="booking-list">
                {page.content.map(user => (
                  <li className="event-row" key={user.id}>
                    <div><span className="booking-title">{user.username}</span><span className="booking-meta">{user.email} · {user.role.toLowerCase()}</span></div>
                    <span className={user.active ? 'status-pill' : 'status-pill status-pill-muted'}>{user.active ? 'active' : 'inactive'}</span>
                    <div className="row-actions"><button className="button button-outline" type="button" onClick={() => edit(user)}>Edit</button><button className="button button-outline" type="button" onClick={() => void toggleActive(user)}>{user.active ? 'Deactivate' : 'Activate'}</button></div>
                  </li>
                ))}
              </ul>
              <Pager page={page} onChange={setPageNumber} />
            </>
          )}
        </section>
      </div>
    </div>
  );
}
