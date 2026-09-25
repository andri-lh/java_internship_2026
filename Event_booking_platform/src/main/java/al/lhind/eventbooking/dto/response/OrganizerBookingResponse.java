package al.lhind.eventbooking.dto.response;

import java.time.LocalDateTime;

public record OrganizerBookingResponse(
        Long id,
        Long attendeeId,
        String attendeeUsername,
        Integer seatsBooked,
        String status,
        LocalDateTime bookingDate,
        Long eventId,
        String eventTitle
) {}