package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.response.EventResponse;
import al.lhind.eventbooking.dto.response.EventSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    Page<EventSummaryResponse> searchPublishedEvents(
            String city,
            LocalDateTime startsAfter,
            LocalDateTime startsBefore,
            BigDecimal minimumPrice,
            BigDecimal maximumPrice,
            Long categoryId,
            Pageable pageable
    );

    EventResponse getPublishedEventById(Long eventId);

    Page<EventSummaryResponse> getAllEventsForAdmin(Pageable pageable);

    EventResponse getEventForAdmin(Long eventId);
}