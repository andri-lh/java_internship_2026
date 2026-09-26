import { useAuth } from '../features/auth/AuthContext';
import { roleHome } from '../features/auth/roleHome';
import { MessagePage } from './MessagePage';

export function ForbiddenPage() {
  const { session } = useAuth();
  const home = session ? roleHome(session.role) : null;

  return (
    <MessagePage
      title="You cannot open this page"
      description="This area is available to a different account role."
      actions={[
        ...(home ? [{ to: home.path, label: home.label }] : [{ to: '/login', label: 'Sign in' }]),
        { to: '/events', label: 'Explore events' },
      ]}
    />
  );
}
