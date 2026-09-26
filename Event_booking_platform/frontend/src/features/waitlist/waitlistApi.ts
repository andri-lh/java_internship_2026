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
