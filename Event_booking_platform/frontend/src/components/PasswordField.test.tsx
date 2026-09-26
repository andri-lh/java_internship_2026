import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { afterEach, describe, expect, it } from 'vitest';
import { isStrongPassword } from '../features/auth/passwordRules';
import { PasswordField } from './PasswordField';

afterEach(cleanup);

function Harness() {
  const [value, setValue] = useState('');
  return <PasswordField value={value} onChange={setValue} autoComplete="new-password" showCriteria />;
}

describe('PasswordField', () => {
  it('toggles between hidden and visible text', async () => {
    render(<Harness />);
    const input = screen.getByLabelText(/password/i, { selector: 'input' });
    expect(input).toHaveProperty('type', 'password');

    await userEvent.click(screen.getByRole('button', { name: 'Show' }));
    expect(input).toHaveProperty('type', 'text');
    expect(screen.getByRole('button', { name: 'Hide' })).toBeTruthy();
  });

  it('updates the criteria checklist live as the password is typed', async () => {
    render(<Harness />);
    const input = screen.getByLabelText(/password/i, { selector: 'input' });

    await userEvent.type(input, 'abc');
    expect(screen.getByText(/At least 8 characters/).closest('li')?.className).toBe('rule-unmet');
    expect(screen.getByText(/A lowercase letter/).closest('li')?.className).toBe('rule-met');

    await userEvent.type(input, 'DEFG1234!');
    expect(screen.getByText(/At least 8 characters/).closest('li')?.className).toBe('rule-met');
    expect(screen.getByText(/A special character/).closest('li')?.className).toBe('rule-met');
  });
});

describe('isStrongPassword', () => {
  it('requires every rule', () => {
    expect(isStrongPassword('Password123!')).toBe(true);
    expect(isStrongPassword('password123!')).toBe(false);
    expect(isStrongPassword('Password!!!!')).toBe(false);
    expect(isStrongPassword('Pass1!')).toBe(false);
    expect(isStrongPassword('A1!' + 'a'.repeat(70))).toBe(false);
  });
});
