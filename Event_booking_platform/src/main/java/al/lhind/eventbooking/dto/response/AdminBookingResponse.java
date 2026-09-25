package al.lhind.eventbooking.dto.response;

import java.time.LocalDateTime;

public record AdminBookingResponse(
        Long id,
        Integer seatsBooked,
        String status,
        LocalDateTime bookingDate,
        Long attendeeId,
        String attendeeUsername,
        Long eventId,
        String eventTitle,
        Long organizerId,
        String organizerUsername
) {}