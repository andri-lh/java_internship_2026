package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import al.lhind.eventbooking.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Events", description = "Browse published events and manage event records according to your role.")
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Search published events", description = "Filter by category, city, date, and price; supports pagination and sorting.")
    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> searchEvents(
            @RequestParam(name = "city", required = false)
            String city,

            @RequestParam(name = "startsAfter", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startsAfter,

            @RequestParam(name = "startsBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startsBefore,

            @RequestParam(name = "minimumPrice", required = false)
            BigDecimal minimumPrice,

            @RequestParam(name = "maximumPrice", required = false)
            BigDecimal maximumPrice,

            @RequestParam(name = "categoryId", required = false)
            Long categoryId,

            @PageableDefault(
                    size = 20,
                    sort = "startDateTime",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<EventSummaryResponse> events =
                eventService.searchPublishedEvents(
                        city,
                        startsAfter,
                        startsBefore,
                        minimumPrice,
                        maximumPrice,
                        categoryId,
                        pageable);

        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get published event details", description = "Includes venue, organizer, categories, and average rating.")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getPublishedEventById(
            @PathVariable Long eventId) {
        EventResponse response = eventService.getPublishedEventById(eventId);
        return ResponseEntity.ok(response);
    }
}