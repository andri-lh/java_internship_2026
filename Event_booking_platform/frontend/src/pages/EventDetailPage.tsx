import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { EventActions } from '../components/EventActions';
import { getPublishedEvent } from '../features/events/eventApi';
import { formatEventDate, formatPrice } from '../features/events/format';
import type { EventDetail } from '../features/events/types';
import { ApiError } from '../services/apiClient';

type LoadState = 'loading' | 'ready' | 'notFound' | 'error';

export function EventDetailPage() {
  const { eventId = '' } = useParams();
  const [event, setEvent] = useState<EventDetail | null>(null);
  const [state, setState] = useState<LoadState>('loading');
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    getPublishedEvent(eventId, controller.signal)
      .then(result => {
        setEvent(result);
        setState('ready');
      })
      .catch(failure => {
        if (controller.signal.aborted) return;
        setState(failure instanceof ApiError && failure.status === 404 ? 'notFound' : 'error');
      });
    return () => controller.abort();
  }, [eventId, refresh]);

  useEffect(() => {
    setState('loading');
  }, [eventId]);

  if (state === 'loading') return <div className="container detail-page"><div className="status-panel">Loading event…</div></div>;

  if (state === 'notFound') {
    return (
      <div className="container detail-page">
        <div className="status-panel"><strong>This event is not available.</strong><p>It may have been cancelled or is not published yet.</p><Link className="button button-outline" to="/events">Back to events</Link></div>
      </div>
    );
  }

  if (state === 'error' || !event) {
    return (
      <div className="container detail-page">
        <div className="status-panel"><strong>We could not load this event.</strong><p>Check that the backend is running, then try again.</p><button className="button button-outline" type="button" onClick={() => window.location.reload()}>Try again</button></div>
      </div>
    );
  }

  const available = event.availableSeats > 0;

  return (
    <div className="container detail-page">
      <Link className="text-link back-link" to="/events"><span aria-hidden="true">←</span> All events</Link>

      <div className="detail-layout">
        <article className="detail-main">
          <div className="event-tags">{event.categories.map(category => <span className="event-tag" key={category}>{category}</span>)}</div>
          <h1>{event.title}</h1>
          <p className="detail-description">{event.description}</p>

          <dl className="detail-facts">
            <div><dt>Starts</dt><dd>{formatEventDate(event.startDateTime)}</dd></div>
            <div><dt>Ends</dt><dd>{formatEventDate(event.endDateTime)}</dd></div>
            <div><dt>Venue</dt><dd>{event.venueName}</dd></div>
            <div><dt>Address</dt><dd>{event.venueAddress}, {event.city}</dd></div>
            <div><dt>Organizer</dt><dd>{event.organizerUsername}</dd></div>
            <div><dt>Rating</dt><dd>{event.averageRating == null ? 'Not rated yet' : event.averageRating.toFixed(1) + ' / 5'}</dd></div>
          </dl>
        </article>

        <aside className="detail-side" aria-label="Booking summary">
          <span className="event-price"><small>Price per seat</small>{formatPrice(event.price)}</span>
          <span className={available ? 'availability' : 'availability sold-out'}>
            <span className="availability-dot" aria-hidden="true" />
            {available ? event.availableSeats + ' of ' + event.totalSeats + ' seats left' : 'Fully booked'}
          </span>
          <EventActions event={event} onChanged={() => setRefresh(value => value + 1)} />
        </aside>
      </div>
    </div>
  );
}
