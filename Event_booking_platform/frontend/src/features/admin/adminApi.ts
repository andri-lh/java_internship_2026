import { apiRequest } from '../../services/apiClient';
import type { Role } from '../auth/session';
import type { Category, EventSummary, PageResponse } from '../events/types';
import type { Venue } from '../organizer/organizerApi';

export interface VenuePayload {
  name: string;
  address: string;
  city: string;
  capacity: number;
}

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: Role;
  active: boolean;
}

export interface UserCreatePayload {
  username: string;
  email: string;
  password: string;
  role: Role;
}

export type UserUpdatePayload = Omit<UserCreatePayload, 'password'>;

export interface AdminBooking {
  id: number;
  seatsBooked: number;
  status: string;
  bookingDate: string;
  attendeeId: number;
  attendeeUsername: string;
  eventId: number;
  eventTitle: string;
  organizerId: number;
  organizerUsername: string;
}

const json = (body: unknown) => JSON.stringify(body);

export const listAdminVenues = (signal?: AbortSignal) => apiRequest<Venue[]>('/admin/venues', { signal });
export const createVenue = (payload: VenuePayload) => apiRequest<Venue>('/admin/venues', { method: 'POST', body: json(payload) });
export const updateVenue = (id: number, payload: VenuePayload) => apiRequest<Venue>('/admin/venues/' + id, { method: 'PUT', body: json(payload) });
export const deleteVenue = (id: number) => apiRequest<void>('/admin/venues/' + id, { method: 'DELETE' });

export const listAdminCategories = (signal?: AbortSignal) => apiRequest<Category[]>('/admin/categories', { signal });
export const createCategory = (name: string) => apiRequest<Category>('/admin/categories', { method: 'POST', body: json({ name }) });
export const updateCategory = (id: number, name: string) => apiRequest<Category>('/admin/categories/' + id, { method: 'PUT', body: json({ name }) });
export const deleteCategory = (id: number) => apiRequest<void>('/admin/categories/' + id, { method: 'DELETE' });

export const listUsers = (page: number, signal?: AbortSignal) => apiRequest<PageResponse<AdminUser>>('/admin/users?size=10&page=' + page, { signal });
export const createUser = (payload: UserCreatePayload) => apiRequest<AdminUser>('/admin/users', { method: 'POST', body: json(payload) });
export const updateUser = (id: number, payload: UserUpdatePayload) => apiRequest<AdminUser>('/admin/users/' + id, { method: 'PUT', body: json(payload) });
export const setUserActive = (id: number, active: boolean) => apiRequest<AdminUser>('/admin/users/' + id + '/activation', { method: 'PATCH', body: json({ active }) });

export const listAdminBookings = (page: number, signal?: AbortSignal) => apiRequest<PageResponse<AdminBooking>>('/admin/bookings?size=10&page=' + page, { signal });
export const cancelAdminBooking = (id: number) => apiRequest<AdminBooking>('/admin/bookings/' + id + '/cancel', { method: 'PATCH' });

export const listAdminEvents = (page: number, signal?: AbortSignal) => apiRequest<PageResponse<EventSummary>>('/admin/events?size=10&page=' + page, { signal });
