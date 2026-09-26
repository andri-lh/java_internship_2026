import { apiRequest } from '../../services/apiClient';

export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Booking {
  id: number;
  seatsBooked: number;
  status: BookingStatus;
  bookingDate: string;
  eventId: number;
  eventTitle: string;
}

export function createBooking(eventId: number, seatsBooked: number) {
  return apiRequest<Booking>('/bookings/events/' + eventId, { method: 'POST', body: JSON.stringify({ seatsBooked }) });
}

export function listMyBookings(status: BookingStatus | '', signal?: AbortSignal) {
  return apiRequest<Booking[]>('/bookings' + (status ? '?status=' + status : ''), { signal });
}

export function cancelBooking(bookingId: number) {
  return apiRequest<Booking>('/bookings/' + bookingId, { method: 'DELETE' });
}
