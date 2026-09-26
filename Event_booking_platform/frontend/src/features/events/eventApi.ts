import { apiRequest } from '../../services/apiClient';
import type { Category, EventDetail, EventSummary, PageResponse } from './types';

export function getPublishedEvent(eventId: string, signal?: AbortSignal) {
  return apiRequest<EventDetail>('/events/' + encodeURIComponent(eventId), { signal });
}

export function searchPublishedEvents(params: URLSearchParams, signal?: AbortSignal) {
  const query = params.toString();
  return apiRequest<PageResponse<EventSummary>>('/events' + (query ? '?' + query : ''), { signal });
}

export function listCategories(signal?: AbortSignal) {
  return apiRequest<Category[]>('/categories', { signal });
}
