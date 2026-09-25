package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.response.OrganizerBookingResponse;
import java.util.List;

public interface OrganizerBookingService {
    List<OrganizerBookingResponse> getBookings(String username);
}