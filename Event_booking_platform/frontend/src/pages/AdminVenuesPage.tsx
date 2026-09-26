import { useEffect, useState, type FormEvent } from 'react';
import { createVenue, deleteVenue, listAdminVenues, updateVenue } from '../features/admin/adminApi';
import { errorText } from '../features/admin/errors';
import type { Venue } from '../features/organizer/organizerApi';
import { ApiError } from '../services/apiClient';

const emptyForm = { name: '', address: '', city: '', capacity: '' };

export function AdminVenuesPage() {
  const [venues, setVenues] = useState<Venue[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    listAdminVenues(controller.signal)
      .then(result => {
        setVenues(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [reload]);

  function reset() {
    setEditingId(null);
    setForm(emptyForm);
    setFieldErrors({});
  }

  function edit(venue: Venue) {
    setEditingId(venue.id);
    setForm({ name: venue.name, address: venue.address, city: venue.city, capacity: String(venue.capacity) });
    setFieldErrors({});
    setMessage(null);
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const found: Record<string, string> = {};
    if (!form.name.trim()) found.name = 'Enter a name.';
    if (!form.address.trim()) found.address = 'Enter an address.';
    if (!form.city.trim()) found.city = 'Enter a city.';
    if (!Number.isInteger(Number(form.capacity)) || Number(form.capacity) < 1) found.capacity = 'Enter a capacity of at least 1.';
    setFieldErrors(found);
    if (Object.keys(found).length > 0) return;

    const payload = { name: form.name.trim(), address: form.address.trim(), city: form.city.trim(), capacity: Number(form.capacity) };
    setBusy(true);
    setMessage(null);
    try {
      if (editingId === null) await createVenue(payload);
      else await updateVenue(editingId, payload);
      setMessage({ error: false, text: editingId === null ? 'Venue created.' : 'Venue updated.' });
      reset();
      setReload(value => value + 1);
    } catch (failure) {
      if (failure instanceof ApiError && Object.keys(failure.validationErrors).length > 0) setFieldErrors(failure.validationErrors);
      else setMessage({ error: true, text: errorText(failure, 'The venue could not be saved. Please try again.') });
    } finally {
      setBusy(false);
    }
  }

  async function remove(venue: Venue) {
    if (!window.confirm('Remove "' + venue.name + '"?')) return;
    setMessage(null);
    try {
      await deleteVenue(venue.id);
      if (editingId === venue.id) reset();
      setMessage({ error: false, text: 'Venue removed.' });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: errorText(failure, 'The venue could not be removed. Please try again.') });
    }
  }

  const set = (key: keyof typeof emptyForm, value: string) => setForm(current => ({ ...current, [key]: value }));
  const fieldError = (key: string) => fieldErrors[key] && <em className="field-error">{fieldErrors[key]}</em>;

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Administration</span><h1>Venues</h1><p>Places where events can take place.</p></div>
      <div className="admin-layout">
        <form className="filter-panel admin-form" onSubmit={submit} noValidate>
          <div className="filter-panel-header"><h2>{editingId === null ? 'New venue' : 'Edit venue'}</h2>{editingId !== null && <button className="link-button" type="button" onClick={reset}>Cancel edit</button>}</div>
          <label className="field"><span>Name</span><input type="text" maxLength={160} value={form.name} onChange={change => set('name', change.target.value)} aria-invalid={!!fieldErrors.name} />{fieldError('name')}</label>
          <label className="field"><span>Address</span><input type="text" maxLength={255} value={form.address} onChange={change => set('address', change.target.value)} aria-invalid={!!fieldErrors.address} />{fieldError('address')}</label>
          <label className="field"><span>City</span><input type="text" maxLength={100} value={form.city} onChange={change => set('city', change.target.value)} aria-invalid={!!fieldErrors.city} />{fieldError('city')}</label>
          <label className="field"><span>Capacity</span><input type="number" min="1" step="1" value={form.capacity} onChange={change => set('capacity', change.target.value)} aria-invalid={!!fieldErrors.capacity} />{fieldError('capacity')}</label>
          <button className="button button-primary filter-submit" type="submit" disabled={busy}>{busy ? 'Saving…' : editingId === null ? 'Create venue' : 'Save changes'}</button>
        </form>

        <section>
          {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
          {state === 'loading' && <div className="status-panel">Loading venues…</div>}
          {state === 'error' && <div className="status-panel"><strong>We could not load the venues.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
          {state === 'ready' && venues.length === 0 && <div className="status-panel"><strong>No venues yet.</strong><p>Create the first venue with the form.</p></div>}
          {state === 'ready' && venues.length > 0 && (
            <ul className="booking-list">
              {venues.map(venue => (
                <li className="event-row admin-row" key={venue.id}>
                  <div><span className="booking-title">{venue.name}</span><span className="booking-meta">{venue.address}, {venue.city} · capacity {venue.capacity}</span></div>
                  <div className="row-actions"><button className="button button-outline" type="button" onClick={() => edit(venue)}>Edit</button><button className="button button-outline" type="button" onClick={() => void remove(venue)}>Remove</button></div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
