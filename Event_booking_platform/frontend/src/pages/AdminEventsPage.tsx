import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Pager } from '../components/Pager';
import { listAdminEvents } from '../features/admin/adminApi';
import { formatEventDate, formatPrice } from '../features/events/format';
import type { EventSummary, PageResponse } from '../features/events/types';

export function AdminEventsPage() {
  const [pageNumber, setPageNumber] = useState(0);
  const [page, setPage] = useState<PageResponse<EventSummary> | null>(null);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    listAdminEvents(pageNumber, controller.signal)
      .then(result => {
        setPage(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [pageNumber, reload]);

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Administration</span><h1>All events</h1><p>Every event in the system, whatever its organizer or status.</p></div>
      {state === 'loading' && <div className="status-panel">Loading events…</div>}
      {state === 'error' && <div className="status-panel"><strong>We could not load the events.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
      {state === 'ready' && page && page.content.length === 0 && <div className="status-panel"><strong>No events yet.</strong></div>}
      {state === 'ready' && page && page.content.length > 0 && (
        <>
          <ul className="booking-list">
            {page.content.map(event => (
              <li className="event-row" key={event.id}>
                <div>
                  <span className="booking-title">{event.title}</span>
                  <span className="booking-meta">{formatEventDate(event.startDateTime)} · {event.venueName}, {event.city}</span>
                  <span className="booking-meta">Organizer: {event.organizerUsername} · {event.availableSeats} of {event.totalSeats} seats available · {formatPrice(event.price)}</span>
                </div>
                <span className={'status-pill status-' + event.status.toLowerCase()}>{event.status.toLowerCase()}</span>
                <div className="row-actions">{event.status === 'PUBLISHED' && <Link className="button button-outline" to={'/events/' + event.id}>View</Link>}</div>
              </li>
            ))}
          </ul>
          <Pager page={page} onChange={setPageNumber} />
        </>
      )}
    </div>
  );
}
