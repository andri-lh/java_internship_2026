import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { formatEventDate, formatPrice } from '../features/events/format';
import { cancelEvent, listMyEvents, listVenues, publishEvent, type OrganizerEvent, type Venue } from '../features/organizer/organizerApi';
import { ApiError } from '../services/apiClient';

export function OrganizerEventsPage() {
  const [events, setEvents] = useState<OrganizerEvent[]>([]);
  const [venues, setVenues] = useState<Venue[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([listMyEvents(controller.signal), listVenues(controller.signal)])
      .then(([eventList, venueList]) => {
        setEvents(eventList);
        setVenues(venueList);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [reload]);

  async function act(event: OrganizerEvent, action: (id: number) => Promise<unknown>, success: string) {
    setBusyId(event.id);
    setMessage(null);
    try {
      await action(event.id);
      setMessage({ error: false, text: success });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: failure instanceof ApiError && failure.status < 500 ? failure.message : 'The action could not be completed. Please try again.' });
    } finally {
      setBusyId(null);
    }
  }

  function cancel(event: OrganizerEvent) {
    if (!window.confirm('Cancel "' + event.title + '"? All confirmed bookings and waitlist entries will be cancelled.')) return;
    void act(event, cancelEvent, 'Event cancelled.');
  }

  const venueName = (id: number) => venues.find(venue => venue.id === id)?.name ?? 'Venue #' + id;

  return (
    <div className="container browse-page">
      <div className="browse-heading page-heading-row">
        <div><span className="eyebrow">Organizer</span><h1>My events</h1><p>Create, publish, and manage the events you own.</p></div>
        <Link className="button button-primary" to="/organizer/events/new">New event</Link>
      </div>

      {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
      {state === 'loading' && <div className="status-panel">Loading your events…</div>}
      {state === 'error' && <div className="status-panel"><strong>We could not load your events.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
      {state === 'ready' && events.length === 0 && <div className="status-panel"><strong>You have not created any events yet.</strong><p>Start with a draft, then publish it when it is ready.</p><Link className="button button-outline" to="/organizer/events/new">Create your first event</Link></div>}
      {state === 'ready' && events.length > 0 && (
        <ul className="booking-list">
          {events.map(event => (
            <li className="event-row" key={event.id}>
              <div>
                <span className="booking-title">{event.title}</span>
                <span className="booking-meta">{formatEventDate(event.startDateTime)} · {venueName(event.venueId)}</span>
                <span className="booking-meta">{event.availableSeats} of {event.totalSeats} seats available · {formatPrice(event.price)} per seat</span>
              </div>
              <span className={'status-pill status-' + event.status.toLowerCase()}>{event.status.toLowerCase()}</span>
              <div className="row-actions">
                {event.status === 'PUBLISHED' && <Link className="button button-outline" to={'/events/' + event.id}>View</Link>}
                {event.status !== 'CANCELLED' && <Link className="button button-outline" to={'/organizer/events/' + event.id + '/edit'}>Edit</Link>}
                {event.status === 'DRAFT' && <button className="button button-primary" type="button" disabled={busyId === event.id} onClick={() => void act(event, publishEvent, 'Event published.')}>Publish</button>}
                {event.status !== 'CANCELLED' && <button className="button button-outline" type="button" disabled={busyId === event.id} onClick={() => cancel(event)}>Cancel</button>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
