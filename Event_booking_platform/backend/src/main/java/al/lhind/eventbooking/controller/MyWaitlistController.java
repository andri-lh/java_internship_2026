package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.config.OpenApiConfig;
import al.lhind.eventbooking.dto.response.WaitlistResponse;
import al.lhind.eventbooking.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Waitlist", description = "Join the waitlist for a sold-out event.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/waitlist")
public class MyWaitlistController {

    private final WaitlistService waitlistService;

    public MyWaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @Operation(summary = "List my waitlist entries")
    @GetMapping
    public ResponseEntity<List<WaitlistResponse>> getMyEntries(Authentication authentication) {
        return ResponseEntity.ok(waitlistService.getMyEntries(authentication.getName()));
    }

    @Operation(summary = "Leave a waitlist", description = "Only entries that are still waiting can be left.")
    @DeleteMapping("/{entryId}")
    public ResponseEntity<WaitlistResponse> leave(
            @PathVariable Long entryId,
            Authentication authentication) {
        return ResponseEntity.ok(waitlistService.leaveWaitlist(authentication.getName(), entryId));
    }
}
