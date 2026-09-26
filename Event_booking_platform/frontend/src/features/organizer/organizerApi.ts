import { apiRequest } from '../../services/apiClient';

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED';

export interface OrganizerEvent {
  id: number;
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  price: number;
  totalSeats: number;
  availableSeats: number;
  status: EventStatus;
  venueId: number;
  categoryIds: number[];
  imageUrl?: string | null;
}

export interface EventPayload {
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  price: number;
  totalSeats: number;
  venueId: number;
  categoryIds: number[];
  imageUrl: string | null;
}

export interface Venue {
  id: number;
  name: string;
  address: string;
  city: string;
  capacity: number;
}

export interface OrganizerBooking {
  id: number;
  attendeeId: number;
  attendeeUsername: string;
  seatsBooked: number;
  status: string;
  bookingDate: string;
  eventId: number;
  eventTitle: string;
}

export function listMyEvents(signal?: AbortSignal) {
  return apiRequest<OrganizerEvent[]>('/organizer/events', { signal });
}

export function getMyEvent(eventId: string, signal?: AbortSignal) {
  return apiRequest<OrganizerEvent>('/organizer/events/' + encodeURIComponent(eventId), { signal });
}

export function createEvent(payload: EventPayload) {
  return apiRequest<OrganizerEvent>('/organizer/events', { method: 'POST', body: JSON.stringify(payload) });
}

export function updateEvent(eventId: number, payload: EventPayload) {
  return apiRequest<OrganizerEvent>('/organizer/events/' + eventId, { method: 'PUT', body: JSON.stringify(payload) });
}

export function publishEvent(eventId: number) {
  return apiRequest<OrganizerEvent>('/organizer/events/' + eventId + '/publish', { method: 'PATCH' });
}

export function cancelEvent(eventId: number) {
  return apiRequest<OrganizerEvent>('/organizer/events/' + eventId + '/cancel', { method: 'PATCH' });
}

export function listVenues(signal?: AbortSignal) {
  return apiRequest<Venue[]>('/organizer/venues', { signal });
}

export function listOrganizerBookings(signal?: AbortSignal) {
  return apiRequest<OrganizerBooking[]>('/organizer/bookings', { signal });
}
