package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.WaitlistResponse;
import al.lhind.eventbooking.service.WaitlistService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Waitlist", description = "Join the waitlist for a sold-out event.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/events/{eventId}/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @Operation(summary = "Join an event waitlist", description = "Available only when a published event has no remaining seats.")
    @PostMapping
    public ResponseEntity<WaitlistResponse> joinWaitlist(
            @PathVariable Long eventId,
            Authentication authentication) {

        WaitlistResponse response = waitlistService.joinWaitlist(
                authentication.getName(),
                eventId
        );

        return ResponseEntity.status(201).body(response);
    }
}