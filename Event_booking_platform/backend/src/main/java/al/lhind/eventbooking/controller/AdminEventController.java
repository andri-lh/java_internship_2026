package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import al.lhind.eventbooking.service.EventService;
import al.lhind.eventbooking.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Events", description = "Browse published events and manage event records according to your role.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "List all events")
    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> getAll(
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(
                eventService.getAllEventsForAdmin(pageable));
    }

    @Operation(summary = "Get any event")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getById(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(
                eventService.getEventForAdmin(eventId));
    }
}