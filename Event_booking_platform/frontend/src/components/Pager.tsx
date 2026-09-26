import type { PageResponse } from '../features/events/types';

export function Pager({ page, onChange }: { page: PageResponse<unknown>; onChange: (number: number) => void }) {
  if (page.totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button className="button button-outline" type="button" disabled={page.first} onClick={() => onChange(page.number - 1)}>← Previous</button>
      <span>Page {page.number + 1} of {page.totalPages}</span>
      <button className="button button-outline" type="button" disabled={page.last} onClick={() => onChange(page.number + 1)}>Next →</button>
    </div>
  );
}
