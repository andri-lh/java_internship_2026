export interface PasswordRule {
  label: string;
  test: (value: string) => boolean;
}

export const passwordRules: PasswordRule[] = [
  { label: 'At least 8 characters', test: value => value.length >= 8 },
  { label: 'No more than 72 characters', test: value => value.length <= 72 },
  { label: 'An uppercase letter', test: value => /[A-Z]/.test(value) },
  { label: 'A lowercase letter', test: value => /[a-z]/.test(value) },
  { label: 'A number', test: value => /\d/.test(value) },
  { label: 'A special character', test: value => /[^A-Za-z0-9]/.test(value) },
];

export const isStrongPassword = (value: string) => passwordRules.every(rule => rule.test(value));
