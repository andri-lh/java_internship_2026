import type { Role } from './session';

const homes: Record<Role, { path: string; label: string }> = {
  ADMIN: { path: '/admin/events', label: 'Go to administration' },
  ORGANIZER: { path: '/organizer/events', label: 'Go to my events' },
  ATTENDEE: { path: '/my/bookings', label: 'Go to my bookings' },
};

export function roleHome(role: Role) {
  return homes[role];
}
