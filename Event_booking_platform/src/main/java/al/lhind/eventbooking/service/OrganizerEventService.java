package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.EventCreateRequest;
import al.lhind.eventbooking.dto.response.OrganizerEventResponse;

public interface OrganizerEventService {

    OrganizerEventResponse createEvent(
            String username, EventCreateRequest request);

    OrganizerEventResponse updateEvent(
            String username, Long eventId, EventCreateRequest request);

    OrganizerEventResponse publishEvent(
            String username, Long eventId);

    OrganizerEventResponse cancelEvent(
            String username, Long eventId);
}