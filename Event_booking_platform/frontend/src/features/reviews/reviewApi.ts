import { apiRequest } from '../../services/apiClient';

export interface Review {
  id: number;
  eventId: number;
  username: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}

export function createReview(eventId: number, rating: number, comment: string) {
  return apiRequest<Review>('/events/' + eventId + '/reviews', {
    method: 'POST',
    body: JSON.stringify({ rating, comment: comment.trim() || null }),
  });
}
