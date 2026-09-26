import { useId, useState } from 'react';
import { passwordRules } from '../features/auth/passwordRules';

interface PasswordFieldProps {
  value: string;
  onChange: (value: string) => void;
  autoComplete: 'current-password' | 'new-password';
  label?: string;
  error?: string;
  showCriteria?: boolean;
}

export function PasswordField({ value, onChange, autoComplete, label = 'Password', error, showCriteria = false }: PasswordFieldProps) {
  const [visible, setVisible] = useState(false);
  const criteriaId = useId();

  return (
    <div className="field">
      <label className="password-label">
        <span>{label}</span>
        <span className="password-input">
          <input
            type={visible ? 'text' : 'password'}
            autoComplete={autoComplete}
            maxLength={72}
            value={value}
            onChange={change => onChange(change.target.value)}
            aria-invalid={!!error}
            aria-describedby={showCriteria ? criteriaId : undefined}
          />
          <button className="password-toggle" type="button" onClick={() => setVisible(current => !current)} aria-pressed={visible}>
            {visible ? 'Hide' : 'Show'}
          </button>
        </span>
      </label>
      {error && <em className="field-error">{error}</em>}
      {showCriteria && (
        <ul className="password-rules" id={criteriaId} aria-live="polite">
          {passwordRules.map(rule => {
            const met = rule.test(value);
            return (
              <li className={met ? 'rule-met' : value ? 'rule-unmet' : ''} key={rule.label}>
                <span aria-hidden="true">{met ? '✓' : '○'}</span> {rule.label}
                <span className="visually-hidden">{met ? ' (met)' : ' (not met)'}</span>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
