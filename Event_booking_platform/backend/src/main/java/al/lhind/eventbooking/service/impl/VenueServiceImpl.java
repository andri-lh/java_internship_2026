package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.exception.ResourceNotFoundException;
import al.lhind.eventbooking.dto.request.VenueRequest;
import al.lhind.eventbooking.dto.response.VenueResponse;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.service.VenueService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

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
        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue created: venueId={}, capacity={}", savedVenue.getId(), savedVenue.getCapacity());
        return toResponse(savedVenue);
    }

    @Override
    @Transactional
    public VenueResponse update(Long venueId, VenueRequest request) {
        Venue venue = requireVenue(venueId);

        if (eventRepository.existsByVenueIdAndTotalSeatsGreaterThan(
                venueId, request.capacity())) {
            throw new BusinessConflictException("Venue capacity is below the seat count of an existing event");
        }

        applyRequest(venue, request);
        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue updated: venueId={}, capacity={}", savedVenue.getId(), savedVenue.getCapacity());
        return toResponse(savedVenue);
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
            throw new BusinessConflictException("Venue cannot be removed because it has events");
        }

        try {
            venueRepository.delete(venue);
            venueRepository.flush();
            log.info("Venue deleted: venueId={}", venueId);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException("Venue cannot be removed because it is in use",
                    exception);
        }
    }

    private Venue requireVenue(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
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