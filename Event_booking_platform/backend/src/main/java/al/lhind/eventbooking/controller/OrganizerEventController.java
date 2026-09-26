package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.EventCreateRequest;
import al.lhind.eventbooking.dto.response.OrganizerEventResponse;
import al.lhind.eventbooking.service.OrganizerEventService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Events", description = "Browse published events and manage event records according to your role.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/organizer/events")
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    public OrganizerEventController(
            OrganizerEventService organizerEventService) {
        this.organizerEventService = organizerEventService;
    }

    @Operation(summary = "List my events", description = "Returns every event owned by the authenticated organizer, including drafts.")
    @GetMapping
    public ResponseEntity<List<OrganizerEventResponse>> getMyEvents(
            Authentication authentication) {
        return ResponseEntity.ok(
                organizerEventService.getMyEvents(authentication.getName()));
    }

    @Operation(summary = "Get one of my events")
    @GetMapping("/{eventId}")
    public ResponseEntity<OrganizerEventResponse> getMyEvent(
            Authentication authentication,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(
                organizerEventService.getMyEvent(authentication.getName(), eventId));
    }

    @Operation(summary = "Create a draft event")
    @PostMapping
    public ResponseEntity<OrganizerEventResponse> createEvent(
            Authentication authentication,
            @Valid @RequestBody EventCreateRequest request) {

        OrganizerEventResponse response =
                organizerEventService.createEvent(
                        authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update my event")
    @PutMapping("/{eventId}")
    public ResponseEntity<OrganizerEventResponse> updateEvent(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request) {

        return ResponseEntity.ok(
                organizerEventService.updateEvent(
                        authentication.getName(), eventId, request));
    }

    @Operation(summary = "Publish my event")
    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<OrganizerEventResponse> publishEvent(
            Authentication authentication,
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                organizerEventService.publishEvent(
                        authentication.getName(), eventId));
    }

    @Operation(summary = "Cancel my event")
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<OrganizerEventResponse> cancelEvent(
            Authentication authentication,
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                organizerEventService.cancelEvent(
                        authentication.getName(), eventId));
    }
}