import { apiRequest } from '../../services/apiClient';

export interface WaitlistEntry {
  id: number;
  eventId: number;
  eventTitle: string;
  status: string;
  joinedAt: string;
}

export function joinWaitlist(eventId: number) {
  return apiRequest<WaitlistEntry>('/events/' + eventId + '/waitlist', { method: 'POST' });
}

export function listMyWaitlist(signal?: AbortSignal) {
  return apiRequest<WaitlistEntry[]>('/waitlist', { signal });
}

export function leaveWaitlist(entryId: number) {
  return apiRequest<WaitlistEntry>('/waitlist/' + entryId, { method: 'DELETE' });
}
