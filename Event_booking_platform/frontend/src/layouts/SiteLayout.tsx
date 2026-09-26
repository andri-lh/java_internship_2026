import { NavLink, Outlet } from 'react-router';
import { useAuth } from '../features/auth/AuthContext';

export function SiteLayout() {
  const { session, signOut } = useAuth();

  return (
    <div className="site-shell">
      <a className="skip-link" href="#main-content">Skip to content</a>
      <header className="site-header">
        <div className="container header-inner">
          <NavLink className="brand" to="/" aria-label="EventBooking home">
            <span className="brand-mark" aria-hidden="true">E</span>
            <span>Event<span className="brand-light">Booking</span></span>
          </NavLink>

          <nav className="main-nav" aria-label="Main navigation">
            <NavLink end to="/" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Home</NavLink>
            <NavLink to="/events" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Explore events</NavLink>
            {session?.role === 'ADMIN' && (
              <>
                <NavLink to="/admin/events" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Events</NavLink>
                <NavLink to="/admin/bookings" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Bookings</NavLink>
                <NavLink to="/admin/users" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Users</NavLink>
                <NavLink to="/admin/venues" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Venues</NavLink>
                <NavLink to="/admin/categories" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Categories</NavLink>
              </>
            )}
            {session?.role === 'ORGANIZER' && (
              <>
                <NavLink to="/organizer/events" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>My events</NavLink>
                <NavLink to="/organizer/bookings" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Bookings</NavLink>
              </>
            )}
            {session?.role === 'ATTENDEE' && <NavLink to="/my/bookings" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>My bookings</NavLink>}
          </nav>

          <div className="header-actions">
            {session ? (
              <>
                <span className="role-badge">{session.role.toLowerCase()}</span>
                <button className="button button-quiet header-signout" type="button" onClick={signOut}>Sign out</button>
              </>
            ) : (
              <>
                <NavLink className="header-login" to="/login">Log in</NavLink>
                <NavLink className="button button-primary header-register" to="/register">Create account</NavLink>
              </>
            )}
          </div>
        </div>
      </header>

      <main id="main-content"><Outlet /></main>

      <footer className="site-footer">
        <div className="container footer-inner">
          <span className="footer-brand">EventBooking</span>
          <span>Good events bring people together.</span>
          <span>© {new Date().getFullYear()} EventBooking</span>
        </div>
      </footer>
    </div>
  );
}
