import { apiRequest } from '../../services/apiClient';
import type { Role } from './session';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  email: string;
}

interface AuthResponse {
  accessToken: string;
  tokenType: string;
  role: Role;
}

export function login(payload: LoginPayload) {
  return apiRequest<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
}

export function register(payload: RegisterPayload) {
  return apiRequest<unknown>('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
}

interface MessageResponse {
  message: string;
}

export function requestPasswordReset(email: string) {
  return apiRequest<MessageResponse>('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) });
}

export function resetPassword(token: string, newPassword: string) {
  return apiRequest<MessageResponse>('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword }) });
}

export function verifyEmail(token: string) {
  return apiRequest<MessageResponse>('/auth/verify-email', { method: 'POST', body: JSON.stringify({ token }) });
}

export function resendVerification(email: string) {
  return apiRequest<MessageResponse>('/auth/resend-verification', { method: 'POST', body: JSON.stringify({ email }) });
}

export function changePassword(currentPassword: string, newPassword: string) {
  return apiRequest<MessageResponse>('/account/password', { method: 'POST', body: JSON.stringify({ currentPassword, newPassword }) });
}
