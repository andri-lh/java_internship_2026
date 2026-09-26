import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Pager } from '../components/Pager';
import { cancelAdminBooking, listAdminBookings, type AdminBooking } from '../features/admin/adminApi';
import { errorText } from '../features/admin/errors';
import { formatEventDate } from '../features/events/format';
import type { PageResponse } from '../features/events/types';

export function AdminBookingsPage() {
  const [pageNumber, setPageNumber] = useState(0);
  const [page, setPage] = useState<PageResponse<AdminBooking> | null>(null);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    listAdminBookings(pageNumber, controller.signal)
      .then(result => {
        setPage(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [pageNumber, reload]);

  async function cancel(booking: AdminBooking) {
    if (!window.confirm('Cancel the booking by ' + booking.attendeeUsername + ' for "' + booking.eventTitle + '"?')) return;
    setCancellingId(booking.id);
    setMessage(null);
    try {
      await cancelAdminBooking(booking.id);
      setMessage({ error: false, text: 'Booking cancelled.' });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: errorText(failure, 'The booking could not be cancelled. Please try again.') });
    } finally {
      setCancellingId(null);
    }
  }

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Administration</span><h1>All bookings</h1><p>Every reservation in the system, newest first.</p></div>
      {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
      {state === 'loading' && <div className="status-panel">Loading bookings…</div>}
      {state === 'error' && <div className="status-panel"><strong>We could not load the bookings.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
      {state === 'ready' && page && page.content.length === 0 && <div className="status-panel"><strong>No bookings yet.</strong></div>}
      {state === 'ready' && page && page.content.length > 0 && (
        <>
          <ul className="booking-list">
            {page.content.map(booking => (
              <li className="event-row" key={booking.id}>
                <div>
                  <span className="booking-title">{booking.eventTitle}</span>
                  <span className="booking-meta">{booking.attendeeUsername} · {booking.seatsBooked} {booking.seatsBooked === 1 ? 'seat' : 'seats'} · booked {formatEventDate(booking.bookingDate)}</span>
                  <span className="booking-meta">Organizer: {booking.organizerUsername}</span>
                </div>
                <span className={booking.status === 'CONFIRMED' ? 'status-pill' : 'status-pill status-pill-muted'}>{booking.status.toLowerCase()}</span>
                <div className="row-actions">
                  <Link className="button button-outline" to={'/events/' + booking.eventId}>Event</Link>
                  {booking.status === 'CONFIRMED' && <button className="button button-outline" type="button" disabled={cancellingId === booking.id} onClick={() => void cancel(booking)}>{cancellingId === booking.id ? 'Cancelling…' : 'Cancel'}</button>}
                </div>
              </li>
            ))}
          </ul>
          <Pager page={page} onChange={setPageNumber} />
        </>
      )}
    </div>
  );
}
