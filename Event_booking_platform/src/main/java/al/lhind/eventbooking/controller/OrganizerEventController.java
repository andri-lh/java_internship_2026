package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.EventCreateRequest;
import al.lhind.eventbooking.dto.response.OrganizerEventResponse;
import al.lhind.eventbooking.service.OrganizerEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizer/events")
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    public OrganizerEventController(
            OrganizerEventService organizerEventService) {
        this.organizerEventService = organizerEventService;
    }

    @PostMapping
    public ResponseEntity<OrganizerEventResponse> createEvent(
            Authentication authentication,
            @Valid @RequestBody EventCreateRequest request) {

        OrganizerEventResponse response =
                organizerEventService.createEvent(
                        authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<OrganizerEventResponse> updateEvent(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request) {

        return ResponseEntity.ok(
                organizerEventService.updateEvent(
                        authentication.getName(), eventId, request));
    }

    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<OrganizerEventResponse> publishEvent(
            Authentication authentication,
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                organizerEventService.publishEvent(
                        authentication.getName(), eventId));
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<OrganizerEventResponse> cancelEvent(
            Authentication authentication,
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                organizerEventService.cancelEvent(
                        authentication.getName(), eventId));
    }
}