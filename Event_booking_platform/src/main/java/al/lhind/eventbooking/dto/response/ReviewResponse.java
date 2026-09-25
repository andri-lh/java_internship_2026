package al.lhind.eventbooking.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long eventId,
        String username,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}