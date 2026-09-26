import { Route, Routes } from 'react-router';
import { SiteLayout } from '../layouts/SiteLayout';
import { AdminBookingsPage } from '../pages/AdminBookingsPage';
import { AdminCategoriesPage } from '../pages/AdminCategoriesPage';
import { AdminEventsPage } from '../pages/AdminEventsPage';
import { AdminUsersPage } from '../pages/AdminUsersPage';
import { AdminVenuesPage } from '../pages/AdminVenuesPage';
import { EventDetailPage } from '../pages/EventDetailPage';
import { EventsPage } from '../pages/EventsPage';
import { ForgotPasswordPage } from '../pages/ForgotPasswordPage';
import { ResetPasswordPage } from '../pages/ResetPasswordPage';
import { ForbiddenPage } from '../pages/ForbiddenPage';
import { HomePage } from '../pages/HomePage';
import { LoginPage } from '../pages/LoginPage';
import { MessagePage } from '../pages/MessagePage';
import { MyBookingsPage } from '../pages/MyBookingsPage';
import { OrganizerBookingsPage } from '../pages/OrganizerBookingsPage';
import { OrganizerEventFormPage } from '../pages/OrganizerEventFormPage';
import { OrganizerEventsPage } from '../pages/OrganizerEventsPage';
import { RegisterPage } from '../pages/RegisterPage';
import { RequireRole } from './RequireRole';

const attendee = ['ATTENDEE'] as const;
const organizer = ['ORGANIZER'] as const;
const admin = ['ADMIN'] as const;

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<SiteLayout />}>
        <Route index element={<HomePage />} />
        <Route path="events" element={<EventsPage />} />
        <Route path="events/:eventId" element={<EventDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="forgot-password" element={<ForgotPasswordPage />} />
        <Route path="reset-password" element={<ResetPasswordPage />} />

        <Route path="my/bookings" element={<RequireRole roles={[...attendee]}><MyBookingsPage /></RequireRole>} />

        <Route path="organizer/events" element={<RequireRole roles={[...organizer]}><OrganizerEventsPage /></RequireRole>} />
        <Route path="organizer/events/new" element={<RequireRole roles={[...organizer]}><OrganizerEventFormPage /></RequireRole>} />
        <Route path="organizer/events/:eventId/edit" element={<RequireRole roles={[...organizer]}><OrganizerEventFormPage /></RequireRole>} />
        <Route path="organizer/bookings" element={<RequireRole roles={[...organizer]}><OrganizerBookingsPage /></RequireRole>} />

        <Route path="admin/venues" element={<RequireRole roles={[...admin]}><AdminVenuesPage /></RequireRole>} />
        <Route path="admin/categories" element={<RequireRole roles={[...admin]}><AdminCategoriesPage /></RequireRole>} />
        <Route path="admin/users" element={<RequireRole roles={[...admin]}><AdminUsersPage /></RequireRole>} />
        <Route path="admin/bookings" element={<RequireRole roles={[...admin]}><AdminBookingsPage /></RequireRole>} />
        <Route path="admin/events" element={<RequireRole roles={[...admin]}><AdminEventsPage /></RequireRole>} />

        <Route path="forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<MessagePage title="Page not found" description="The address may have changed or the page does not exist." actions={[{ to: '/', label: 'Back to home' }, { to: '/events', label: 'Explore events' }]} />} />
      </Route>
    </Routes>
  );
}
