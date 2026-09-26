package al.lhind.eventbooking.dto.response;

import al.lhind.eventbooking.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OrganizerEventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        BigDecimal price,
        Integer totalSeats,
        Integer availableSeats,
        EventStatus status,
        Long venueId,
        Set<Long> categoryIds,
        String imageUrl
) {}