import { useEffect, useState, type FormEvent } from 'react';
import { createCategory, deleteCategory, listAdminCategories, updateCategory } from '../features/admin/adminApi';
import { errorText } from '../features/admin/errors';
import type { Category } from '../features/events/types';
import { ApiError } from '../services/apiClient';

export function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [name, setName] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    listAdminCategories(controller.signal)
      .then(result => {
        setCategories(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [reload]);

  function reset() {
    setEditingId(null);
    setName('');
    setFieldError('');
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim()) {
      setFieldError('Enter a name.');
      return;
    }
    setFieldError('');
    setBusy(true);
    setMessage(null);
    try {
      if (editingId === null) await createCategory(name.trim());
      else await updateCategory(editingId, name.trim());
      setMessage({ error: false, text: editingId === null ? 'Category created.' : 'Category updated.' });
      reset();
      setReload(value => value + 1);
    } catch (failure) {
      if (failure instanceof ApiError && failure.validationErrors.name) setFieldError(failure.validationErrors.name);
      else setMessage({ error: true, text: errorText(failure, 'The category could not be saved. Please try again.') });
    } finally {
      setBusy(false);
    }
  }

  async function remove(category: Category) {
    if (!window.confirm('Remove "' + category.name + '"?')) return;
    setMessage(null);
    try {
      await deleteCategory(category.id);
      if (editingId === category.id) reset();
      setMessage({ error: false, text: 'Category removed.' });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: errorText(failure, 'The category could not be removed. Please try again.') });
    }
  }

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Administration</span><h1>Categories</h1><p>Labels that help attendees find events.</p></div>
      <div className="admin-layout">
        <form className="filter-panel admin-form" onSubmit={submit} noValidate>
          <div className="filter-panel-header"><h2>{editingId === null ? 'New category' : 'Edit category'}</h2>{editingId !== null && <button className="link-button" type="button" onClick={reset}>Cancel edit</button>}</div>
          <label className="field"><span>Name</span><input type="text" maxLength={100} value={name} onChange={change => setName(change.target.value)} aria-invalid={!!fieldError} />{fieldError && <em className="field-error">{fieldError}</em>}</label>
          <button className="button button-primary filter-submit" type="submit" disabled={busy}>{busy ? 'Saving…' : editingId === null ? 'Create category' : 'Save changes'}</button>
        </form>

        <section>
          {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
          {state === 'loading' && <div className="status-panel">Loading categories…</div>}
          {state === 'error' && <div className="status-panel"><strong>We could not load the categories.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
          {state === 'ready' && categories.length === 0 && <div className="status-panel"><strong>No categories yet.</strong><p>Create the first category with the form.</p></div>}
          {state === 'ready' && categories.length > 0 && (
            <ul className="booking-list">
              {categories.map(category => (
                <li className="event-row admin-row" key={category.id}>
                  <span className="booking-title">{category.name}</span>
                  <div className="row-actions"><button className="button button-outline" type="button" onClick={() => { setEditingId(category.id); setName(category.name); setFieldError(''); setMessage(null); }}>Edit</button><button className="button button-outline" type="button" onClick={() => void remove(category)}>Remove</button></div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
