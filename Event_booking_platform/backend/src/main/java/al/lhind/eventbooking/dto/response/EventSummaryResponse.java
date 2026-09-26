package al.lhind.eventbooking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record EventSummaryResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        BigDecimal price,
        Integer totalSeats,
        Integer availableSeats,
        String status,
        String venueName,
        String city,
        String organizerUsername,
        Set<String> categories,
        String imageUrl
) {
}