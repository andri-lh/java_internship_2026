package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.AdminBookingResponse;
import al.lhind.eventbooking.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminBookingResponse>> getAll(
            Authentication authentication,
            @PageableDefault(
                    size = 20,
                    sort = "bookingDate",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                bookingService.getAllBookingsForAdmin(
                        authentication.getName(), pageable));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<AdminBookingResponse> getById(
            Authentication authentication,
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.getBookingForAdmin(
                        authentication.getName(), bookingId));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<AdminBookingResponse> cancel(
            Authentication authentication,
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.cancelBookingForAdmin(
                        authentication.getName(), bookingId));
    }
}