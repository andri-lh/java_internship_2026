package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.EventCreateRequest;
import al.lhind.eventbooking.dto.response.OrganizerEventResponse;
import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.service.OrganizerEventService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrganizerEventServiceImpl implements OrganizerEventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;

    public OrganizerEventServiceImpl(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            WaitlistEntryRepository waitlistEntryRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
    }

    @Override
    @Transactional
    public OrganizerEventResponse createEvent(
            String username, EventCreateRequest request) {

        User organizer = requireOrganizer(username);
        validateSchedule(request);

        Venue venue = requireVenue(request.venueId());
        validateCapacity(request.totalSeats(), venue);
        Set<Category> categories = requireCategories(request.categoryIds());

        Event event = new Event();
        event.setOrganizer(organizer);
        applyDetails(event, request, venue, categories);
        event.setAvailableSeats(request.totalSeats());
        event.setStatus(EventStatus.DRAFT);

        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public OrganizerEventResponse updateEvent(
            String username, Long eventId, EventCreateRequest request) {

        User organizer = requireOrganizer(username);
        Event event = requireOwnedEventForUpdate(eventId, organizer);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw conflict("Cancelled events cannot be updated");
        }

        if (!event.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw conflict("Started events cannot be updated");
        }

        if (event.getStatus() == EventStatus.PUBLISHED
                && bookingRepository.existsByEventIdAndStatus(
                eventId, BookingStatus.CONFIRMED)) {
            throw conflict(
                    "Published events with confirmed bookings cannot be updated");
        }

        validateSchedule(request);

        Venue venue = requireVenue(request.venueId());
        validateCapacity(request.totalSeats(), venue);
        Set<Category> categories = requireCategories(request.categoryIds());

        applyDetails(event, request, venue, categories);
        event.setAvailableSeats(request.totalSeats());

        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public OrganizerEventResponse publishEvent(
            String username, Long eventId) {

        User organizer = requireOrganizer(username);
        Event event = requireOwnedEventForUpdate(eventId, organizer);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw conflict("Only draft events can be published");
        }

        if (!event.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw conflict("The event must start in the future");
        }

        if (!event.getEndDateTime().isAfter(event.getStartDateTime())) {
            throw conflict("The end must be after the start");
        }

        validateCapacity(event.getTotalSeats(), event.getVenue());

        event.setStatus(EventStatus.PUBLISHED);
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public OrganizerEventResponse cancelEvent(
            String username, Long eventId) {

        User organizer = requireOrganizer(username);
        Event event = requireOwnedEventForUpdate(eventId, organizer);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw conflict("Event is already cancelled");
        }

        if (!event.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw conflict("Started events cannot be cancelled");
        }

        List<Booking> confirmedBookings =
                bookingRepository.findByEventIdAndStatus(
                        eventId, BookingStatus.CONFIRMED);

        for (Booking booking : confirmedBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }
        bookingRepository.saveAll(confirmedBookings);

        List<WaitlistEntry> waitingEntries =
                waitlistEntryRepository
                        .findByEventIdAndStatusOrderByJoinedAtAsc(
                                eventId, WaitlistStatus.WAITING);

        for (WaitlistEntry entry : waitingEntries) {
            entry.setStatus(WaitlistStatus.CANCELLED);
        }
        waitlistEntryRepository.saveAll(waitingEntries);

        event.setStatus(EventStatus.CANCELLED);
        event.setAvailableSeats(event.getTotalSeats());

        return toResponse(eventRepository.save(event));
    }

    private User requireOrganizer(String username) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Active user not found"));

        if (user.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only organizers can manage events");
        }

        return user;
    }

    private Event requireOwnedEventForUpdate(Long eventId, User organizer) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        return event;
    }

    private Venue requireVenue(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Venue not found"));
    }

    private Set<Category> requireCategories(Set<Long> categoryIds) {
        List<Category> found = categoryRepository.findAllById(categoryIds);

        if (found.size() != categoryIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "One or more categories were not found");
        }

        return new HashSet<>(found);
    }

    private void validateSchedule(EventCreateRequest request) {
        if (!request.startDateTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "The event must start in the future");
        }

        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "The end must be after the start");
        }
    }

    private void validateCapacity(Integer totalSeats, Venue venue) {
        if (totalSeats > venue.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total seats cannot exceed venue capacity");
        }
    }

    private void applyDetails(
            Event event,
            EventCreateRequest request,
            Venue venue,
            Set<Category> categories) {

        event.setTitle(request.title().trim());
        event.setDescription(request.description().trim());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setPrice(request.price());
        event.setTotalSeats(request.totalSeats());
        event.setVenue(venue);
        event.setCategories(categories);
    }

    private OrganizerEventResponse toResponse(Event event) {
        return new OrganizerEventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getPrice(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getStatus(),
                event.getVenue().getId(),
                event.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet())
        );
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}