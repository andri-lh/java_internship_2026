import { useEffect, useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router';
import { EventCard } from '../components/EventCard';
import { listCategories, searchPublishedEvents } from '../features/events/eventApi';
import type { Category, EventSummary, PageResponse } from '../features/events/types';

interface Filters {
  city: string;
  categoryId: string;
  fromDate: string;
  toDate: string;
  minimumPrice: string;
  maximumPrice: string;
  sort: string;
}

function filtersFromUrl(params: URLSearchParams): Filters {
  return {
    city: params.get('city') || '',
    categoryId: params.get('categoryId') || '',
    fromDate: (params.get('startsAfter') || '').slice(0, 10),
    toDate: (params.get('startsBefore') || '').slice(0, 10),
    minimumPrice: params.get('minimumPrice') || '',
    maximumPrice: params.get('maximumPrice') || '',
    sort: params.get('sort') || 'startDateTime,asc',
  };
}

export function EventsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.toString();
  const [filters, setFilters] = useState<Filters>(() => filtersFromUrl(searchParams));
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoryError, setCategoryError] = useState(false);
  const [page, setPage] = useState<PageResponse<EventSummary> | null>(null);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    setFilters(filtersFromUrl(new URLSearchParams(query)));
  }, [query]);

  useEffect(() => {
    const controller = new AbortController();
    listCategories(controller.signal)
      .then(setCategories)
      .catch(() => {
        if (!controller.signal.aborted) setCategoryError(true);
      });
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const params = new URLSearchParams(query);
    if (!params.has('size')) params.set('size', '9');
    if (!params.has('sort')) params.set('sort', 'startDateTime,asc');
    setState('loading');

    searchPublishedEvents(params, controller.signal)
      .then(result => {
        setPage(result);
        setState('ready');
      })
      .catch(() => {
        if (!controller.signal.aborted) setState('error');
      });

    return () => controller.abort();
  }, [query]);

  function update<K extends keyof Filters>(key: K, value: Filters[K]) {
    setFilters(current => ({ ...current, [key]: value }));
  }

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (filters.fromDate && filters.toDate && filters.fromDate > filters.toDate) {
      setValidationError('The first date must be before the last date.');
      return;
    }
    if (filters.minimumPrice && filters.maximumPrice && Number(filters.minimumPrice) > Number(filters.maximumPrice)) {
      setValidationError('Minimum price cannot exceed maximum price.');
      return;
    }

    setValidationError('');
    const next = new URLSearchParams();
    if (filters.city.trim()) next.set('city', filters.city.trim());
    if (filters.categoryId) next.set('categoryId', filters.categoryId);
    if (filters.fromDate) next.set('startsAfter', filters.fromDate + 'T00:00:00');
    if (filters.toDate) next.set('startsBefore', filters.toDate + 'T23:59:59');
    if (filters.minimumPrice) next.set('minimumPrice', filters.minimumPrice);
    if (filters.maximumPrice) next.set('maximumPrice', filters.maximumPrice);
    next.set('sort', filters.sort);
    next.set('page', '0');
    setSearchParams(next);
  }

  function clearFilters() {
    setValidationError('');
    setSearchParams(new URLSearchParams());
  }

  function goToPage(number: number) {
    const next = new URLSearchParams(query);
    next.set('page', String(number));
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  return (
    <div className="container browse-page">
      <div className="browse-heading"><span className="eyebrow">The event collection</span><h1>Find your next event.</h1><p>Use the filters to discover gatherings that match your plans.</p></div>

      <div className="browse-layout">
        <aside className="filter-panel" aria-label="Event filters">
          <div className="filter-panel-header"><h2>Filters</h2><button className="link-button" type="button" onClick={clearFilters}>Clear all</button></div>
          <form onSubmit={applyFilters}>
            <label className="field"><span>City</span><input type="text" placeholder="Any city" value={filters.city} onChange={event => update('city', event.target.value)} /></label>
            <label className="field"><span>Category</span><select value={filters.categoryId} onChange={event => update('categoryId', event.target.value)} disabled={categoryError}><option value="">All categories</option>{categories.map(category => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
            {categoryError && <p className="field-hint">Categories are unavailable right now.</p>}
            <div className="filter-divider" />
            <div className="field-group-label">Date range</div>
            <label className="field"><span>From</span><input type="date" value={filters.fromDate} onChange={event => update('fromDate', event.target.value)} /></label>
            <label className="field"><span>To</span><input type="date" value={filters.toDate} onChange={event => update('toDate', event.target.value)} /></label>
            <div className="filter-divider" />
            <div className="field-group-label">Price range</div>
            <div className="field-pair"><label className="field"><span>Minimum</span><input type="number" min="0" step="0.01" placeholder="0" value={filters.minimumPrice} onChange={event => update('minimumPrice', event.target.value)} /></label><label className="field"><span>Maximum</span><input type="number" min="0" step="0.01" placeholder="Any" value={filters.maximumPrice} onChange={event => update('maximumPrice', event.target.value)} /></label></div>
            <label className="field"><span>Sort by</span><select value={filters.sort} onChange={event => update('sort', event.target.value)}><option value="startDateTime,asc">Date: soonest</option><option value="startDateTime,desc">Date: latest</option><option value="price,asc">Price: low to high</option><option value="price,desc">Price: high to low</option><option value="title,asc">Title: A to Z</option></select></label>
            {validationError && <p className="form-error" role="alert">{validationError}</p>}
            <button className="button button-primary filter-submit" type="submit">Show events</button>
          </form>
        </aside>

        <section className="browse-results" aria-live="polite">
          <div className="results-heading"><div><h2>Published events</h2><p>{state === 'ready' && page ? page.totalElements + ' events found' : 'Find something to look forward to'}</p></div><span className="results-indicator">Explore at your pace</span></div>
          {state === 'loading' && <div className="status-panel">Loading events…</div>}
          {state === 'error' && <div className="status-panel"><strong>We could not load the events.</strong><p>Check that the backend is running, then try again.</p><button className="button button-outline" type="button" onClick={() => window.location.reload()}>Try again</button></div>}
          {state === 'ready' && page?.content.length === 0 && <div className="status-panel"><strong>No events match these filters.</strong><p>Try a different city, date, or price range.</p><button className="button button-outline" type="button" onClick={clearFilters}>Clear filters</button></div>}
          {state === 'ready' && page && page.content.length > 0 && <><div className="event-grid results-grid">{page.content.map((event, index) => <EventCard event={event} index={index} key={event.id} />)}</div><div className="pagination"><button className="button button-outline" type="button" disabled={page.first} onClick={() => goToPage(page.number - 1)}>← Previous</button><span>Page {page.number + 1} of {page.totalPages}</span><button className="button button-outline" type="button" disabled={page.last} onClick={() => goToPage(page.number + 1)}>Next →</button></div></>}
        </section>
      </div>
    </div>
  );
}
