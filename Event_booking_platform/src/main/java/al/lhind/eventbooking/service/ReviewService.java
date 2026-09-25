package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.ReviewCreateRequest;
import al.lhind.eventbooking.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(
            String username,
            Long eventId,
            ReviewCreateRequest request
    );
}