import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { EventCard } from '../components/EventCard';
import { searchPublishedEvents } from '../features/events/eventApi';
import { localDateTimeNow } from '../features/events/format';
import type { EventSummary } from '../features/events/types';

export function HomePage() {
  const navigate = useNavigate();
  const [city, setCity] = useState('');
  const [events, setEvents] = useState<EventSummary[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');

  useEffect(() => {
    const controller = new AbortController();
    const params = new URLSearchParams({
      startsAfter: localDateTimeNow(),
      page: '0',
      size: '3',
      sort: 'startDateTime,asc',
    });

    searchPublishedEvents(params, controller.signal)
      .then(page => {
        setEvents(page.content);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });

    return () => controller.abort();
  }, []);

  function searchCity(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const query = city.trim();
    navigate(query ? '/events?city=' + encodeURIComponent(query) : '/events');
  }

  return (
    <>
      <section className="hero-section">
        <div className="container hero-grid">
          <div className="hero-copy">
            <span className="eyebrow"><span className="eyebrow-dot" /> Find something worth showing up for</span>
            <h1>Make room for <span>what matters.</span></h1>
            <p>Thoughtful gatherings, new places, and shared experiences. Discover events that fit your plans and reserve your seat with ease.</p>
            <form className="hero-search" onSubmit={searchCity}>
              <label htmlFor="hero-city">Where would you like to go?</label>
              <div className="hero-search-row">
                <span className="search-icon" aria-hidden="true">⌕</span>
                <input id="hero-city" type="text" placeholder="Search by city" value={city} onChange={event => setCity(event.target.value)} />
                <button className="button button-primary" type="submit">Find events <span aria-hidden="true">↗</span></button>
              </div>
            </form>
            <div className="hero-caption"><span className="caption-line" /> Discover. Reserve. Be there.</div>
          </div>

          <div className="hero-visual" aria-hidden="true">
            <div className="hero-visual-glow" />
            <div className="hero-orbit hero-orbit-one" />
            <div className="hero-orbit hero-orbit-two" />
            <div className="hero-panel">
              <div className="hero-panel-head"><span>THE EXPERIENCE</span><span className="hero-panel-spark">✳</span></div>
              <div className="hero-panel-center"><span>YOUR NEXT<br />MOMENT<br />STARTS HERE</span></div>
              <div className="hero-panel-foot"><span>EVENTBOOKING</span><span>01 / ∞</span></div>
            </div>
            <div className="hero-float-card"><span className="float-icon">✦</span><span><strong>Find your people</strong><small>One event at a time</small></span></div>
          </div>
        </div>
      </section>

      <section className="container section-block" aria-labelledby="upcoming-heading">
        <div className="section-heading">
          <div><span className="eyebrow">Explore what is next</span><h2 id="upcoming-heading">Coming up soon</h2><p>Browse the events people are planning right now.</p></div>
          <Link className="text-link" to="/events">View all events <span aria-hidden="true">↗</span></Link>
        </div>
        {state === 'loading' && <div className="status-panel">Finding upcoming events…</div>}
        {state === 'error' && <div className="status-panel">Events could not be loaded right now. <Link to="/events">Try the event browser</Link>.</div>}
        {state === 'ready' && events.length === 0 && <div className="status-panel">No upcoming events have been published yet. Check back soon.</div>}
        {state === 'ready' && events.length > 0 && <div className="event-grid">{events.map((event, index) => <EventCard event={event} index={index} key={event.id} />)}</div>}
      </section>

      <section className="container how-section" aria-labelledby="how-heading">
        <div className="how-intro"><span className="eyebrow">Simple by design</span><h2 id="how-heading">A better way to be there.</h2></div>
        <div className="how-grid">
          <div className="how-card"><span className="how-number">01</span><h3>Discover</h3><p>Explore published events by city, date, category, and price.</p></div>
          <div className="how-card"><span className="how-number">02</span><h3>Reserve</h3><p>See available seats and make plans when the right event appears.</p></div>
          <div className="how-card"><span className="how-number">03</span><h3>Be there</h3><p>Enjoy the moment and share your experience afterward.</p></div>
        </div>
      </section>
    </>
  );
}
