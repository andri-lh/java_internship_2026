package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import al.lhind.eventbooking.service.EventService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

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

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getPublishedEventById(
            @PathVariable Long eventId) {
        EventResponse response = eventService.getPublishedEventById(eventId);
        return ResponseEntity.ok(response);
    }
}