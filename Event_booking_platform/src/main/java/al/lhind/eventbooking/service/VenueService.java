package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.VenueRequest;
import al.lhind.eventbooking.dto.response.VenueResponse;
import java.util.List;

public interface VenueService {
    VenueResponse create(VenueRequest request);
    VenueResponse update(Long venueId, VenueRequest request);
    VenueResponse getById(Long venueId);
    List<VenueResponse> getAll();
    void delete(Long venueId);
}