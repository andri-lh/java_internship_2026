package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.OrganizerBookingResponse;
import al.lhind.eventbooking.service.OrganizerBookingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizer/bookings")
public class OrganizerBookingController {

    private final OrganizerBookingService organizerBookingService;

    public OrganizerBookingController(
            OrganizerBookingService organizerBookingService) {
        this.organizerBookingService = organizerBookingService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizerBookingResponse>> getBookings(
            Authentication authentication) {
        return ResponseEntity.ok(
                organizerBookingService.getBookings(authentication.getName()));
    }
}