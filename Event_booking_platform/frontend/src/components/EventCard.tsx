import { Link } from 'react-router';
import { formatEventDate, formatPrice } from '../features/events/format';
import type { EventSummary } from '../features/events/types';

export function EventCard({ event, index = 0 }: { event: EventSummary; index?: number }) {
  const date = new Date(event.startDateTime);
  const hasValidDate = !Number.isNaN(date.getTime());
  const month = hasValidDate ? new Intl.DateTimeFormat('en', { month: 'short' }).format(date) : 'Date';
  const day = hasValidDate ? date.getDate() : '—';
  const available = event.availableSeats > 0;

  return (
    <article className="event-card">
      <div className="event-card-art" data-tone={index % 3} style={event.imageUrl ? { backgroundImage: 'linear-gradient(180deg, rgba(0,0,0,.05), rgba(0,0,0,.25)), url("' + event.imageUrl + '")' } : undefined}>
        <span className="art-ring art-ring-one" aria-hidden="true" />
        <span className="art-ring art-ring-two" aria-hidden="true" />
        <span className="event-city">{event.city}</span>
        <span className="event-date-tile" aria-label={formatEventDate(event.startDateTime)}>
          <span>{month}</span>
          <strong>{day}</strong>
        </span>
      </div>

      <div className="event-card-body">
        <div className="event-tags">
          {event.categories.slice(0, 2).map(category => <span className="event-tag" key={category}>{category}</span>)}
          {event.categories.length > 2 && <span className="event-tag">+{event.categories.length - 2}</span>}
        </div>
        <h3><Link to={'/events/' + event.id}>{event.title}</Link></h3>
        <p className="event-description">{event.description}</p>
        <div className="event-meta">
          <span>{formatEventDate(event.startDateTime)}</span>
          <span>{event.venueName}</span>
        </div>
        <div className="event-card-bottom">
          <span className={available ? 'availability' : 'availability sold-out'}>
            <span className="availability-dot" aria-hidden="true" />
            {available ? event.availableSeats + ' seats left' : 'Fully booked'}
          </span>
          <span className="event-price"><small>Price</small>{formatPrice(event.price)}</span>
        </div>
      </div>
    </article>
  );
}
