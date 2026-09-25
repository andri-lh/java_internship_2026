package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.WaitlistResponse;
import al.lhind.eventbooking.service.WaitlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

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