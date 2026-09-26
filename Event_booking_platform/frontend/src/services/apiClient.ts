import { readSession } from '../features/auth/session';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '');

interface ApiErrorBody {
  message?: string;
  validationErrors?: Record<string, string>;
}

let unauthorizedHandler: (() => void) | null = null;

// Called when a request that carried a token is rejected with 401 (expired or revoked session).
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

export class ApiError extends Error {
  readonly status: number;
  readonly validationErrors: Record<string, string>;

  constructor(message: string, status: number, validationErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  if (options.body) {
    headers.set('Content-Type', 'application/json');
  }

  const token = readSession()?.accessToken;
  if (token) {
    headers.set('Authorization', 'Bearer ' + token);
  }

  const response = await fetch(API_BASE_URL + path, { ...options, headers });

  if (!response.ok) {
    if (response.status === 401 && token && !path.startsWith('/auth/')) unauthorizedHandler?.();
    let message = 'The request could not be completed.';
    let validationErrors: Record<string, string> = {};
    try {
      const body = await response.json() as ApiErrorBody;
      if (body.message) message = body.message;
      if (body.validationErrors) validationErrors = body.validationErrors;
    } catch {
      // Keep the fallback message for non-JSON responses.
    }
    throw new ApiError(message, response.status, validationErrors);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
