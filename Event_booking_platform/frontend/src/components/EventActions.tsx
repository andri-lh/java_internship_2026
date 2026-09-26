import { useState, type FormEvent } from 'react';
import { Link, useLocation } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';
import { createBooking } from '../features/bookings/bookingApi';
import { formatPrice } from '../features/events/format';
import type { EventDetail } from '../features/events/types';
import { createReview } from '../features/reviews/reviewApi';
import { joinWaitlist } from '../features/waitlist/waitlistApi';
import { ApiError } from '../services/apiClient';

interface Notice {
  kind: 'success' | 'error';
  text: string;
}

function messageOf(failure: unknown): string {
  return failure instanceof ApiError && failure.status < 500
    ? failure.message
    : 'Something went wrong. Please try again.';
}

export function EventActions({ event, onChanged }: { event: EventDetail; onChanged: () => void }) {
  const { session } = useAuth();
  const location = useLocation();
  const [seats, setSeats] = useState('1');
  const [rating, setRating] = useState('5');
  const [comment, setComment] = useState('');
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<Notice | null>(null);
  const [reviewed, setReviewed] = useState(false);

  const ended = new Date(event.endDateTime).getTime() < Date.now();
  const seatCount = Number(seats);
  const validSeats = Number.isInteger(seatCount) && seatCount >= 1 && seatCount <= event.availableSeats;

  if (!session) {
    return <Link className="button button-primary" to="/login" state={{ from: location }}>Sign in to book</Link>;
  }
  if (session.role !== 'ATTENDEE') {
    return <p className="action-note">Only attendee accounts can book seats or leave reviews.</p>;
  }

  async function run(action: () => Promise<string>) {
    setBusy(true);
    setNotice(null);
    try {
      setNotice({ kind: 'success', text: await action() });
      onChanged();
    } catch (failure) {
      setNotice({ kind: 'error', text: messageOf(failure) });
    } finally {
      setBusy(false);
    }
  }

  function book(submit: FormEvent<HTMLFormElement>) {
    submit.preventDefault();
    void run(async () => {
      await createBooking(event.id, seatCount);
      setSeats('1');
      return 'Booked ' + seatCount + (seatCount === 1 ? ' seat.' : ' seats.') + ' Total ' + formatPrice(event.price * seatCount) + '.';
    });
  }

  function review(submit: FormEvent<HTMLFormElement>) {
    submit.preventDefault();
    void run(async () => {
      await createReview(event.id, Number(rating), comment);
      setReviewed(true);
      return 'Thank you, your review was saved.';
    });
  }

  return (
    <div className="event-actions">
      {!ended && event.availableSeats > 0 && (
        <form onSubmit={book}>
          <label className="field"><span>Seats</span><input type="number" min="1" max={event.availableSeats} step="1" value={seats} onChange={change => setSeats(change.target.value)} /></label>
          {validSeats && <p className="action-note">Total {formatPrice(event.price * seatCount)}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={busy || !validSeats}>{busy ? 'Booking…' : 'Book seats'}</button>
        </form>
      )}

      {!ended && event.availableSeats === 0 && (
        <>
          <p className="action-note">This event is fully booked. Join the waitlist and you will be promoted if a seat opens.</p>
          <button className="button button-primary auth-submit" type="button" disabled={busy} onClick={() => void run(async () => {
            await joinWaitlist(event.id);
            return 'You are on the waitlist.';
          })}>{busy ? 'Joining…' : 'Join waitlist'}</button>
        </>
      )}

      {ended && !reviewed && (
        <form onSubmit={review}>
          <p className="action-note">This event has ended. If you attended, share your experience.</p>
          <label className="field"><span>Rating</span><select value={rating} onChange={change => setRating(change.target.value)}>{[5, 4, 3, 2, 1].map(value => <option key={value} value={value}>{value} / 5</option>)}</select></label>
          <label className="field"><span>Comment (optional)</span><textarea rows={4} maxLength={2000} value={comment} onChange={change => setComment(change.target.value)} /></label>
          <button className="button button-primary auth-submit" type="submit" disabled={busy}>{busy ? 'Saving…' : 'Submit review'}</button>
        </form>
      )}

      {notice && <p className={notice.kind === 'error' ? 'form-error' : 'form-success'} role={notice.kind === 'error' ? 'alert' : 'status'}>{notice.text}</p>}
    </div>
  );
}
