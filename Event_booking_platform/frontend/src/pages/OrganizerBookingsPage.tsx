import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { formatEventDate } from '../features/events/format';
import { listOrganizerBookings, type OrganizerBooking } from '../features/organizer/organizerApi';

export function OrganizerBookingsPage() {
  const [bookings, setBookings] = useState<OrganizerBooking[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [status, setStatus] = useState('');
  const [eventId, setEventId] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setState('loading');
    listOrganizerBookings(controller.signal)
      .then(result => {
        setBookings(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });
    return () => controller.abort();
  }, [reload]);

  const events = Array.from(new Map(bookings.map(booking => [booking.eventId, booking.eventTitle])));
  const visible = bookings.filter(booking => (!status || booking.status === status) && (!eventId || String(booking.eventId) === eventId));

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">Organizer</span><h1>Event bookings</h1><p>Reservations made against the events you own.</p></div>

      <div className="results-heading filter-row">
        <label className="field bookings-filter"><span>Event</span><select value={eventId} onChange={change => setEventId(change.target.value)}><option value="">All events</option>{events.map(([id, title]) => <option key={id} value={id}>{title}</option>)}</select></label>
        <label className="field bookings-filter"><span>Status</span><select value={status} onChange={change => setStatus(change.target.value)}><option value="">All bookings</option><option value="CONFIRMED">Confirmed</option><option value="CANCELLED">Cancelled</option></select></label>
      </div>

      {state === 'loading' && <div className="status-panel">Loading bookings…</div>}
      {state === 'error' && <div className="status-panel"><strong>We could not load the bookings.</strong><button className="button button-outline" type="button" onClick={() => setReload(value => value + 1)}>Try again</button></div>}
      {state === 'ready' && visible.length === 0 && <div className="status-panel"><strong>No bookings found.</strong><p>Bookings appear here once attendees reserve seats.</p></div>}
      {state === 'ready' && visible.length > 0 && (
        <ul className="booking-list">
          {visible.map(booking => (
            <li className="booking-row booking-row-simple" key={booking.id}>
              <div>
                <Link className="booking-title" to={'/events/' + booking.eventId}>{booking.eventTitle}</Link>
                <span className="booking-meta">{booking.attendeeUsername} · {booking.seatsBooked} {booking.seatsBooked === 1 ? 'seat' : 'seats'} · booked {formatEventDate(booking.bookingDate)}</span>
              </div>
              <span className={booking.status === 'CONFIRMED' ? 'status-pill' : 'status-pill status-pill-muted'}>{booking.status.toLowerCase()}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
