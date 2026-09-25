package al.lhind.eventbooking.booking;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.entity.WaitlistEntry;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void bookingRequestPersistsAndUpdatesAvailableSeats() throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/bookings/events/{eventId}",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seatsBooked": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatsBooked").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Event savedEvent = eventRepository.findById(data.event().getId())
                .orElseThrow();
        List<Booking> attendeeBookings =
                bookingRepository.findByUserId(data.attendee().getId());

        assertEquals(3, savedEvent.getAvailableSeats());
        assertEquals(1, attendeeBookings.size());
        assertEquals(2, attendeeBookings.getFirst().getSeatsBooked());
        assertEquals(
                BookingStatus.CONFIRMED,
                attendeeBookings.getFirst().getStatus()
        );

        int confirmedSeats = bookingRepository
                .findByUserIdAndStatus(
                        data.attendee().getId(),
                        BookingStatus.CONFIRMED
                )
                .stream()
                .mapToInt(Booking::getSeatsBooked)
                .sum();

        assertEquals(
                savedEvent.getTotalSeats(),
                savedEvent.getAvailableSeats() + confirmedSeats
        );
    }

    @Test
    void invalidSeatCountIsRejectedByRequestValidation() throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/bookings/events/{eventId}",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seatsBooked": 0}
                                """))
                .andExpect(status().isBadRequest());

        Event savedEvent = eventRepository.findById(data.event().getId())
                .orElseThrow();

        assertEquals(5, savedEvent.getAvailableSeats());
        assertEquals(
                0,
                bookingRepository.findByUserId(data.attendee().getId()).size()
        );
    }

    @Test
    void requestExceedingAvailableSeatsIsRejectedWithoutChangingState()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/bookings/events/{eventId}",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seatsBooked": 6}
                                """))
                .andExpect(status().isConflict());

        Event savedEvent = eventRepository.findById(data.event().getId())
                .orElseThrow();

        assertEquals(5, savedEvent.getAvailableSeats());
        assertEquals(
                0,
                bookingRepository.findByUserId(data.attendee().getId()).size()
        );
    }

    @Test
    void attendeeCanViewOnlyTheirOwnBookingsAndFilterThemByStatus()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        User anotherAttendee = createUser(
                "another-attendee-" + UUID.randomUUID(),
                Role.ATTENDEE
        );

        Booking confirmedBooking = createBooking(
                data.attendee(), data.event(), BookingStatus.CONFIRMED, 2
        );
        Booking cancelledBooking = createBooking(
                data.attendee(), data.event(), BookingStatus.CANCELLED, 1
        );
        Booking anotherUsersBooking = createBooking(
                anotherAttendee, data.event(), BookingStatus.CONFIRMED, 1
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(get("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath(
                        "$[*].id",
                        containsInAnyOrder(
                                confirmedBooking.getId().intValue(),
                                cancelledBooking.getId().intValue()
                        )
                ));

        mockMvc.perform(get("/api/v1/bookings")
                        .param("status", BookingStatus.CANCELLED.name())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id")
                        .value(cancelledBooking.getId().intValue()))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));

        // Ensure another attendee's booking was created so the ownership assertion
        // above proves it was excluded from the authenticated user's response.
        assertEquals(
                anotherAttendee.getId(),
                bookingRepository.findById(anotherUsersBooking.getId())
                        .orElseThrow()
                        .getUser()
                        .getId()
        );
    }

    @Test
    void attendeeCanCancelTheirConfirmedBookingAndSeatsAreRestored()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        Booking booking = createBooking(
                data.attendee(), data.event(), BookingStatus.CONFIRMED, 2
        );
        data.event().setAvailableSeats(3);
        eventRepository.saveAndFlush(data.event());
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(delete("/api/v1/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().intValue()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(
                BookingStatus.CANCELLED,
                bookingRepository.findById(booking.getId())
                        .orElseThrow()
                        .getStatus()
        );
        assertEquals(
                5,
                eventRepository.findById(data.event().getId())
                        .orElseThrow()
                        .getAvailableSeats()
        );
    }

    @Test
    void cancellationIsRejectedWithin24HoursOfEventStart() throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        LocalDateTime start = LocalDateTime.now().plusHours(12);
        data.event().setStartDateTime(start);
        data.event().setEndDateTime(start.plusHours(2));
        data.event().setAvailableSeats(4);
        Event event = eventRepository.saveAndFlush(data.event());
        Booking booking = createBooking(
                data.attendee(), event, BookingStatus.CONFIRMED, 1
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(delete("/api/v1/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());

        assertEquals(
                BookingStatus.CONFIRMED,
                bookingRepository.findById(booking.getId())
                        .orElseThrow()
                        .getStatus()
        );
        assertEquals(
                4,
                eventRepository.findById(data.event().getId())
                        .orElseThrow()
                        .getAvailableSeats()
        );
    }

    @Test
    void attendeeCannotCancelAnotherUsersBooking() throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        User anotherAttendee = createUser(
                "booking-owner-" + UUID.randomUUID(),
                Role.ATTENDEE
        );
        Booking anotherUsersBooking = createBooking(
                anotherAttendee,
                data.event(),
                BookingStatus.CONFIRMED,
                1
        );
        data.event().setAvailableSeats(4);
        eventRepository.saveAndFlush(data.event());
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(delete(
                        "/api/v1/bookings/{bookingId}",
                        anotherUsersBooking.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());

        assertEquals(
                BookingStatus.CONFIRMED,
                bookingRepository.findById(anotherUsersBooking.getId())
                        .orElseThrow()
                        .getStatus()
        );
        assertEquals(
                4,
                eventRepository.findById(data.event().getId())
                        .orElseThrow()
                        .getAvailableSeats()
        );
    }

    @Test
    void cancellingSoldOutEventBookingPromotesNextWaitlistedAttendee()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        data.event().setTotalSeats(1);
        data.event().setAvailableSeats(0);
        eventRepository.saveAndFlush(data.event());

        Booking booking = createBooking(
                data.attendee(), data.event(), BookingStatus.CONFIRMED, 1
        );
        User firstWaitingAttendee = createUser(
                "first-waiter-" + UUID.randomUUID(), Role.ATTENDEE
        );
        User secondWaitingAttendee = createUser(
                "second-waiter-" + UUID.randomUUID(), Role.ATTENDEE
        );
        WaitlistEntry firstEntry = createWaitlistEntry(
                firstWaitingAttendee, data.event(), LocalDateTime.now().minusMinutes(2)
        );
        WaitlistEntry secondEntry = createWaitlistEntry(
                secondWaitingAttendee, data.event(), LocalDateTime.now().minusMinutes(1)
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(delete("/api/v1/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(
                WaitlistStatus.PROMOTED,
                waitlistEntryRepository.findById(firstEntry.getId())
                        .orElseThrow()
                        .getStatus()
        );
        assertEquals(
                WaitlistStatus.WAITING,
                waitlistEntryRepository.findById(secondEntry.getId())
                        .orElseThrow()
                        .getStatus()
        );
        List<Booking> promotedBookings = bookingRepository
                .findByUserId(firstWaitingAttendee.getId());
        assertEquals(1, promotedBookings.size());
        assertEquals(BookingStatus.CONFIRMED, promotedBookings.getFirst().getStatus());
        assertEquals(1, promotedBookings.getFirst().getSeatsBooked());
        assertEquals(
                firstWaitingAttendee.getId(),
                promotedBookings.getFirst().getUser().getId()
        );
        assertEquals(
                0,
                eventRepository.findById(data.event().getId())
                        .orElseThrow()
                        .getAvailableSeats()
        );
    }

    @Test
    void attendeeCanJoinWaitlistForFullyBookedPublishedEvent()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        data.event().setAvailableSeats(0);
        eventRepository.saveAndFlush(data.event());
        User existingBooker = createUser(
                "existing-booker-" + UUID.randomUUID(), Role.ATTENDEE
        );
        createBooking(existingBooker, data.event(), BookingStatus.CONFIRMED, 5);
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/waitlist",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId")
                        .value(data.event().getId().intValue()))
                .andExpect(jsonPath("$.eventTitle")
                        .value(data.event().getTitle()))
                .andExpect(jsonPath("$.status").value("WAITING"));

        WaitlistEntry entry = waitlistEntryRepository
                .findByUserIdAndEventId(
                        data.attendee().getId(), data.event().getId()
                )
                .orElseThrow();
        assertEquals(WaitlistStatus.WAITING, entry.getStatus());
        assertEquals(data.attendee().getId(), entry.getUser().getId());
        assertEquals(data.event().getId(), entry.getEvent().getId());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/waitlist",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void attendeeCannotJoinWaitlistWhileEventHasAvailableSeats()
            throws Exception {
        TestData data = createPublishedEventWithFiveSeats();
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/waitlist",
                        data.event().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());

        assertEquals(
                5,
                eventRepository.findById(data.event().getId())
                        .orElseThrow()
                        .getAvailableSeats()
        );
        assertEquals(
                0L,
                waitlistEntryRepository.findByUserIdAndEventId(
                        data.attendee().getId(), data.event().getId()
                ).stream().count()
        );
    }

    private TestData createPublishedEventWithFiveSeats() {
        String suffix = UUID.randomUUID().toString();
        User organizer = createUser("organizer-" + suffix, Role.ORGANIZER);
        User attendee = createUser("attendee-" + suffix, Role.ATTENDEE);

        Venue venue = new Venue();
        venue.setName("Test Venue " + suffix);
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(5);
        venue = venueRepository.saveAndFlush(venue);

        LocalDateTime start = LocalDateTime.now().plusDays(3);
        Event event = new Event();
        event.setTitle("Test Event " + suffix);
        event.setDescription("Event for booking integration test");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("25.00"));
        event.setTotalSeats(5);
        event.setAvailableSeats(5);
        event.setStatus(EventStatus.PUBLISHED);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event = eventRepository.saveAndFlush(event);

        return new TestData(attendee, event);
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("test-password");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private Booking createBooking(
            User user,
            Event event,
            BookingStatus status,
            int seatsBooked) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setStatus(status);
        booking.setSeatsBooked(seatsBooked);
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepository.saveAndFlush(booking);
    }

    private WaitlistEntry createWaitlistEntry(
            User user,
            Event event,
            LocalDateTime joinedAt) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(user);
        entry.setEvent(event);
        entry.setJoinedAt(joinedAt);
        entry.setStatus(WaitlistStatus.WAITING);
        return waitlistEntryRepository.saveAndFlush(entry);
    }

    private record TestData(User attendee, Event event) {
    }
}
