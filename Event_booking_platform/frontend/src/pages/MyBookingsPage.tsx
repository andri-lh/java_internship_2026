import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { cancelBooking, listMyBookings, type Booking, type BookingStatus } from '../features/bookings/bookingApi';
import { formatEventDate } from '../features/events/format';
import { leaveWaitlist, listMyWaitlist, type WaitlistEntry } from '../features/waitlist/waitlistApi';
import { ApiError } from '../services/apiClient';

export function MyBookingsPage() {
  const [status, setStatus] = useState<BookingStatus | ''>('');
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);

  const [waitlist, setWaitlist] = useState<WaitlistEntry[]>([]);
  const [leavingId, setLeavingId] = useState<number | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    listMyWaitlist(controller.signal).then(setWaitlist).catch(() => {
      if (!controller.signal.aborted) setWaitlist([]);
    });
    return () => controller.abort();
  }, [reload]);

  async function leave(entry: WaitlistEntry) {
    if (!window.confirm('Leave the waitlist for "' + entry.eventTitle + '"?')) return;
    setLeavingId(entry.id);
    setMessage(null);
    try {
      await leaveWaitlist(entry.id);
      setMessage({ error: false, text: 'You left the waitlist.' });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: failure instanceof ApiError && failure.status < 500 ? failure.message : 'Could not leave the waitlist. Please try again.' });
    } finally {
      setLeavingId(null);
    }
  }

  useEffect(() => {
    const controller = new AbortController();
    setState('loading');
    listMyBookings(status, controller.signal)
      .then(result => {
        setBookings(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [status, reload]);

  async function cancel(booking: Booking) {
    if (!window.confirm('Cancel your booking for "' + booking.eventTitle + '"?')) return;
    setCancellingId(booking.id);
    setMessage(null);
    try {
      await cancelBooking(booking.id);
      setMessage({ error: false, text: 'Booking cancelled.' });
      setReload(value => value + 1);
    } catch (failure) {
      setMessage({ error: true, text: failure instanceof ApiError && failure.status < 500 ? failure.message : 'The booking could not be cancelled. Please try again.' });
    } finally {
      setCancellingId(null);
    }
  }

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Your reservations</span><h1>My bookings</h1><p>Confirmed bookings can be cancelled until 24 hours before the event starts.</p></div>

      <div className="results-heading">
        <label className="field bookings-filter"><span>Status</span><select value={status} onChange={change => setStatus(change.target.value as BookingStatus | '')}><option value="">All bookings</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option></select></label>
      </div>

      {message && <p className={message.error ? 'form-error' : 'form-success'} role={message.error ? 'alert' : 'status'}>{message.text}</p>}
      {state === 'loading' && <div className="status-panel">Loading your bookings…</div>}
      {state === 'error' && <div className="status-panel"><strong>We could not load your bookings.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
      {state === 'ready' && bookings.length === 0 && <div className="status-panel"><strong>No bookings found.</strong><p>Find an event and reserve your first seat.</p><Link className="button button-outline" to="/events">Explore events</Link></div>}
      {state === 'ready' && bookings.length > 0 && (
        <ul className="booking-list">
          {bookings.map(booking => (
            <li className="booking-row" key={booking.id}>
              <div>
                <Link className="booking-title" to={'/events/' + booking.eventId}>{booking.eventTitle}</Link>
                <span className="booking-meta">Booked {formatEventDate(booking.bookingDate)} · {booking.seatsBooked} {booking.seatsBooked === 1 ? 'seat' : 'seats'}</span>
              </div>
              <span className={booking.status === 'CONFIRMED' ? 'status-pill' : 'status-pill status-pill-muted'}>{booking.status.toLowerCase()}</span>
              {booking.status === 'CONFIRMED'
                ? <button className="button button-outline" type="button" disabled={cancellingId === booking.id} onClick={() => void cancel(booking)}>{cancellingId === booking.id ? 'Cancelling…' : 'Cancel'}</button>
                : <span />}
            </li>
          ))}
        </ul>
      )}
      {waitlist.length > 0 && (
        <section className="waitlist-section">
          <h2>My waitlist</h2>
          <ul className="booking-list">
            {waitlist.map(entry => (
              <li className="booking-row" key={entry.id}>
                <div>
                  <Link className="booking-title" to={'/events/' + entry.eventId}>{entry.eventTitle}</Link>
                  <span className="booking-meta">Joined {formatEventDate(entry.joinedAt)}</span>
                </div>
                <span className={entry.status === 'WAITING' ? 'status-pill' : 'status-pill status-pill-muted'}>{entry.status.toLowerCase()}</span>
                {entry.status === 'WAITING'
                  ? <button className="button button-outline" type="button" disabled={leavingId === entry.id} onClick={() => void leave(entry)}>{leavingId === entry.id ? 'Leaving…' : 'Leave'}</button>
                  : <span />}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
