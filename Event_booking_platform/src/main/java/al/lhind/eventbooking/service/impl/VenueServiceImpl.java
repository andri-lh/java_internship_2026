package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.VenueRequest;
import al.lhind.eventbooking.dto.response.VenueResponse;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.service.VenueService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueServiceImpl(
            VenueRepository venueRepository,
            EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue();
        applyRequest(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    @Transactional
    public VenueResponse update(Long venueId, VenueRequest request) {
        Venue venue = requireVenue(venueId);

        if (eventRepository.existsByVenueIdAndTotalSeatsGreaterThan(
                venueId, request.capacity())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Venue capacity is below the seat count of an existing event");
        }

        applyRequest(venue, request);
        return toResponse(venueRepository.save(venue));
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse getById(Long venueId) {
        return toResponse(requireVenue(venueId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueResponse> getAll() {
        return venueRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long venueId) {
        Venue venue = requireVenue(venueId);

        if (eventRepository.existsByVenueId(venueId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Venue cannot be removed because it has events");
        }

        try {
            venueRepository.delete(venue);
            venueRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Venue cannot be removed because it is in use",
                    exception);
        }
    }

    private Venue requireVenue(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Venue not found"));
    }

    private void applyRequest(Venue venue, VenueRequest request) {
        venue.setName(request.name().trim());
        venue.setAddress(request.address().trim());
        venue.setCity(request.city().trim());
        venue.setCapacity(request.capacity());
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                venue.getCapacity());
    }
}