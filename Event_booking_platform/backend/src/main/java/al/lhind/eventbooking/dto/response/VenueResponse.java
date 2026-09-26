package al.lhind.eventbooking.dto.response;

public record VenueResponse(
        Long id,
        String name,
        String address,
        String city,
        Integer capacity
) {}