package al.lhind.eventbooking.dto.response;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Integer seatsBooked,
        String status,
        LocalDateTime bookingDate,
        Long eventId,
        String eventTitle) {
}
