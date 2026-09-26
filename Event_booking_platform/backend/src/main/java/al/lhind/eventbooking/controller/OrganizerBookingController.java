package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.OrganizerBookingResponse;
import al.lhind.eventbooking.service.OrganizerBookingService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookings", description = "Create, view, and cancel bookings according to your role.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/organizer/bookings")
public class OrganizerBookingController {

    private final OrganizerBookingService organizerBookingService;

    public OrganizerBookingController(
            OrganizerBookingService organizerBookingService) {
        this.organizerBookingService = organizerBookingService;
    }

    @Operation(summary = "List bookings for my events")
    @GetMapping
    public ResponseEntity<List<OrganizerBookingResponse>> getBookings(
            Authentication authentication) {
        return ResponseEntity.ok(
                organizerBookingService.getBookings(authentication.getName()));
    }
}