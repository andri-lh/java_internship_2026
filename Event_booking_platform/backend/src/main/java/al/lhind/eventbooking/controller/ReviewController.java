package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.ReviewCreateRequest;
import al.lhind.eventbooking.dto.response.ReviewResponse;
import al.lhind.eventbooking.service.ReviewService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reviews", description = "Review events you attended.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/events/{eventId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Review an attended event", description = "Requires a confirmed booking and an event that has ended; one review per attendee per event.")
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {

        ReviewResponse response = reviewService.createReview(
                authentication.getName(),
                eventId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}