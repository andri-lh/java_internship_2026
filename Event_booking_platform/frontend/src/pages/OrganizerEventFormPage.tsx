import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { listCategories } from '../features/events/eventApi';
import type { Category } from '../features/events/types';
import { createEvent, getMyEvent, listVenues, updateEvent, type EventPayload, type Venue } from '../features/organizer/organizerApi';
import { ApiError } from '../services/apiClient';

interface FormState {
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  price: string;
  totalSeats: string;
  venueId: string;
  categoryIds: number[];
}

const emptyForm: FormState = {
  title: '',
  description: '',
  startDateTime: '',
  endDateTime: '',
  price: '',
  totalSeats: '',
  venueId: '',
  categoryIds: [],
};

const toApiDateTime = (value: string) => (value.length === 16 ? value + ':00' : value);

export function OrganizerEventFormPage() {
  const { eventId } = useParams();
  const editing = eventId !== undefined;
  const navigate = useNavigate();

  const [form, setForm] = useState<FormState>(emptyForm);
  const [venues, setVenues] = useState<Venue[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'notFound' | 'error'>('loading');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setState('loading');
    Promise.all([
      listVenues(controller.signal),
      listCategories(controller.signal),
      eventId ? getMyEvent(eventId, controller.signal) : Promise.resolve(null),
    ])
      .then(([venueList, categoryList, event]) => {
        setVenues(venueList);
        setCategories(categoryList);
        if (event) {
          setForm({
            title: event.title,
            description: event.description,
            startDateTime: event.startDateTime.slice(0, 16),
            endDateTime: event.endDateTime.slice(0, 16),
            price: String(event.price),
            totalSeats: String(event.totalSeats),
            venueId: String(event.venueId),
            categoryIds: event.categoryIds,
          });
        }
        setState('ready');
      })
      .catch(failure => {
        if (controller.signal.aborted) return;
        setState(failure instanceof ApiError && failure.status === 404 ? 'notFound' : 'error');
      });
    return () => controller.abort();
  }, [eventId]);

  function update<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm(current => ({ ...current, [key]: value }));
  }

  function toggleCategory(id: number) {
    update('categoryIds', form.categoryIds.includes(id) ? form.categoryIds.filter(value => value !== id) : [...form.categoryIds, id]);
  }

  function validate(): Record<string, string> {
    const found: Record<string, string> = {};
    const venue = venues.find(item => String(item.id) === form.venueId);
    if (!form.title.trim()) found.title = 'Enter a title.';
    else if (form.title.trim().length > 180) found.title = 'Title can have at most 180 characters.';
    if (!form.description.trim()) found.description = 'Enter a description.';
    if (!form.startDateTime) found.startDateTime = 'Choose a start time.';
    else if (new Date(form.startDateTime).getTime() <= Date.now()) found.startDateTime = 'The event must start in the future.';
    if (!form.endDateTime) found.endDateTime = 'Choose an end time.';
    else if (form.startDateTime && form.endDateTime <= form.startDateTime) found.endDateTime = 'The end must be after the start.';
    if (form.price === '' || Number(form.price) < 0) found.price = 'Enter a price of 0 or more.';
    if (!Number.isInteger(Number(form.totalSeats)) || Number(form.totalSeats) < 1) found.totalSeats = 'Enter at least 1 seat.';
    else if (venue && Number(form.totalSeats) > venue.capacity) found.totalSeats = 'Seats cannot exceed the venue capacity of ' + venue.capacity + '.';
    if (!form.venueId) found.venueId = 'Choose a venue.';
    return found;
  }

  async function submit(submitEvent: FormEvent<HTMLFormElement>) {
    submitEvent.preventDefault();
    setError('');
    const found = validate();
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    const payload: EventPayload = {
      title: form.title.trim(),
      description: form.description.trim(),
      startDateTime: toApiDateTime(form.startDateTime),
      endDateTime: toApiDateTime(form.endDateTime),
      price: Number(form.price),
      totalSeats: Number(form.totalSeats),
      venueId: Number(form.venueId),
      categoryIds: form.categoryIds,
    };

    setSubmitting(true);
    try {
      if (eventId) await updateEvent(Number(eventId), payload);
      else await createEvent(payload);
      navigate('/organizer/events');
    } catch (failure) {
      if (failure instanceof ApiError && failure.status < 500) {
        setErrors(failure.validationErrors);
        setError(Object.keys(failure.validationErrors).length > 0 ? '' : failure.message);
      } else {
        setError('The event could not be saved. Please try again.');
      }
      setSubmitting(false);
    }
  }

  if (state === 'loading') return <div className="container browse-page"><div className="status-panel">Loading…</div></div>;
  if (state === 'notFound') return <div className="container browse-page"><div className="status-panel"><strong>Event not found.</strong><p>It may not exist or does not belong to you.</p><Link className="button button-outline" to="/organizer/events">Back to my events</Link></div></div>;
  if (state === 'error') return <div className="container browse-page"><div className="status-panel"><strong>We could not load the form.</strong><button className="button button-outline" type="button" onClick={() => window.location.reload()}>Try again</button></div></div>;

  const fieldError = (key: string) => errors[key] && <em className="field-error">{errors[key]}</em>;

  return (
    <div className="container browse-page">
      <Link className="text-link back-link" to="/organizer/events"><span aria-hidden="true">←</span> My events</Link>
      <div className="browse-heading"><span className="eyebrow">Organizer</span><h1>{editing ? 'Edit event' : 'New event'}</h1><p>{editing ? 'Update the details of your event.' : 'Events start as drafts. Publish them from your event list when ready.'}</p></div>

      <form className="event-form" onSubmit={submit} noValidate>
        <label className="field"><span>Title</span><input type="text" maxLength={180} value={form.title} onChange={change => update('title', change.target.value)} aria-invalid={!!errors.title} />{fieldError('title')}</label>
        <label className="field"><span>Description</span><textarea rows={5} value={form.description} onChange={change => update('description', change.target.value)} aria-invalid={!!errors.description} />{fieldError('description')}</label>
        <div className="field-pair">
          <label className="field"><span>Starts</span><input type="datetime-local" value={form.startDateTime} onChange={change => update('startDateTime', change.target.value)} aria-invalid={!!errors.startDateTime} />{fieldError('startDateTime')}</label>
          <label className="field"><span>Ends</span><input type="datetime-local" value={form.endDateTime} onChange={change => update('endDateTime', change.target.value)} aria-invalid={!!errors.endDateTime} />{fieldError('endDateTime')}</label>
        </div>
        <div className="field-pair">
          <label className="field"><span>Price per seat</span><input type="number" min="0" step="0.01" value={form.price} onChange={change => update('price', change.target.value)} aria-invalid={!!errors.price} />{fieldError('price')}</label>
          <label className="field"><span>Total seats</span><input type="number" min="1" step="1" value={form.totalSeats} onChange={change => update('totalSeats', change.target.value)} aria-invalid={!!errors.totalSeats} />{fieldError('totalSeats')}</label>
        </div>
        <label className="field"><span>Venue</span><select value={form.venueId} onChange={change => update('venueId', change.target.value)} aria-invalid={!!errors.venueId}><option value="">Select a venue</option>{venues.map(venue => <option key={venue.id} value={venue.id}>{venue.name} · {venue.city} (capacity {venue.capacity})</option>)}</select>{fieldError('venueId')}</label>
        <fieldset className="category-picker">
          <legend>Categories</legend>
          {categories.length === 0 && <span className="field-hint">No categories are available yet.</span>}
          {categories.map(category => <label className="check-chip" key={category.id}><input type="checkbox" checked={form.categoryIds.includes(category.id)} onChange={() => toggleCategory(category.id)} /><span>{category.name}</span></label>)}
        </fieldset>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={submitting}>{submitting ? 'Saving…' : editing ? 'Save changes' : 'Create draft'}</button>
          <Link className="button button-outline" to="/organizer/events">Cancel</Link>
        </div>
      </form>
    </div>
  );
}
