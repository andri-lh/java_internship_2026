import { Link } from 'react-router';

interface MessageAction {
  to: string;
  label: string;
}

export function MessagePage({ title, description, actions }: { title: string; description: string; actions?: MessageAction[] }) {
  const buttons = actions ?? [{ to: '/events', label: 'Explore events' }];

  return (
    <section className="container message-page">
      <span className="eyebrow">EventBooking</span>
      <h1>{title}</h1>
      <p>{description}</p>
      <div className="form-actions">
        {buttons.map((action, index) => (
          <Link className={index === 0 ? 'button button-primary' : 'button button-outline'} to={action.to} key={action.to}>{action.label}</Link>
        ))}
      </div>
    </section>
  );
}
