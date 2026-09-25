package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.BookingRequest;
import al.lhind.eventbooking.dto.response.BookingResponse;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{eventId}")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        BookingResponse response = bookingService.createBooking(
                authentication.getName(),
                eventId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            Authentication authentication) {

        List<BookingResponse> bookings =
                bookingService.getMyBookings(authentication.getName(), status);

        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> cancelMyBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {

        BookingResponse response = bookingService.cancelMyBooking(
                authentication.getName(),
                bookingId
        );

        return ResponseEntity.ok(response);
    }
}