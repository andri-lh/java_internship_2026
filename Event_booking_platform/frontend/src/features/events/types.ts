export interface Category {
  id: number;
  name: string;
}

export interface EventSummary {
  id: number;
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  price: number;
  totalSeats: number;
  availableSeats: number;
  status: string;
  venueName: string;
  city: string;
  organizerUsername: string;
  categories: string[];
}

export interface EventDetail extends EventSummary {
  venueAddress: string;
  averageRating: number | null;
}

export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
