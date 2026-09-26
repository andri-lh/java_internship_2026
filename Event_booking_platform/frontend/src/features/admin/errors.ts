import { ApiError } from '../../services/apiClient';

export function errorText(failure: unknown, fallback: string): string {
  return failure instanceof ApiError && failure.status < 500 ? failure.message : fallback;
}
