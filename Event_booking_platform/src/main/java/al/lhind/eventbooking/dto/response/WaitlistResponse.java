package al.lhind.eventbooking.dto.response;

import java.time.LocalDateTime;

public record WaitlistResponse(
        Long id,
        Long eventId,
        String eventTitle,
        String status,
        LocalDateTime joinedAt
) {
}