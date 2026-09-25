package al.lhind.eventbooking.booking;

import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizerBookingApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void organizerSeesConfirmedAndCancelledBookingsAcrossOwnedEventsOnly()
            throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = createUser("owner-" + suffix, Role.ORGANIZER);
        User otherOrganizer = createUser("other-" + suffix, Role.ORGANIZER);
        User alice = createUser("alice-" + suffix, Role.ATTENDEE);
        User bob = createUser("bob-" + suffix, Role.ATTENDEE);
        Venue venue = createVenue(suffix);
        Event firstEvent = createEvent("First show", owner, venue, 8);
        Event secondEvent = createEvent("Second show", owner, venue, 9);
        Event otherEvent = createEvent("Other organizer show", otherOrganizer, venue, 9);
        LocalDateTime bookedAt = LocalDateTime.now().withNano(0);

        Booking oldest = createBooking(alice, firstEvent, BookingStatus.CONFIRMED,
                2, bookedAt.minusDays(2));
        Booking middle = createBooking(bob, firstEvent, BookingStatus.CANCELLED,
                1, bookedAt.minusDays(1));
        Booking newest = createBooking(alice, secondEvent, BookingStatus.CONFIRMED,
                1, bookedAt);
        createBooking(bob, otherEvent, BookingStatus.CONFIRMED,
                1, bookedAt.plusMinutes(1));

        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(newest.getId()))
                .andExpect(jsonPath("$[0].eventId").value(secondEvent.getId()))
                .andExpect(jsonPath("$[0].eventTitle").value("Second show"))
                .andExpect(jsonPath("$[0].attendeeId").value(alice.getId()))
                .andExpect(jsonPath("$[0].attendeeUsername").value(alice.getUsername()))
                .andExpect(jsonPath("$[0].seatsBooked").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].bookingDate").exists())
                .andExpect(jsonPath("$[1].id").value(middle.getId()))
                .andExpect(jsonPath("$[1].eventId").value(firstEvent.getId()))
                .andExpect(jsonPath("$[1].attendeeUsername").value(bob.getUsername()))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"))
                .andExpect(jsonPath("$[2].id").value(oldest.getId()))
                .andExpect(jsonPath("$[2].eventId").value(firstEvent.getId()))
                .andExpect(jsonPath("$[2].seatsBooked").value(2));

        // The fixture contains four bookings, but the owner receives only three.
        assertEquals(4, bookingRepository.count());
    }

    @Test
    void organizerWithNoBookingsGetsEmptyListEvenWhenAnotherOrganizerHasBookings()
            throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = createUser("owner-" + suffix, Role.ORGANIZER);
        User otherOrganizer = createUser("other-" + suffix, Role.ORGANIZER);
        User attendee = createUser("attendee-" + suffix, Role.ATTENDEE);
        Venue venue = createVenue(suffix);
        Event otherEvent = createEvent("Private show", otherOrganizer, venue, 9);
        createBooking(attendee, otherEvent, BookingStatus.CONFIRMED,
                1, LocalDateTime.now());

        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void attendeeAdminAndAnonymousVisitorCannotReadOrganizerBookings()
            throws Exception {
        String suffix = UUID.randomUUID().toString();
        User attendee = createUser("attendee-" + suffix, Role.ATTENDEE);
        User admin = createUser("admin-" + suffix, Role.ADMIN);

        mockMvc.perform(get("/api/v1/organizer/bookings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivatedOrganizerCannotUseExistingToken() throws Exception {
        User owner = createUser("owner-" + UUID.randomUUID(), Role.ORGANIZER);
        String token = bearer(owner);
        owner.setActive(false);
        userRepository.saveAndFlush(owner);

        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("test-password");
        user.setRole(role);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }

    private Venue createVenue(String suffix) {
        Venue venue = new Venue();
        venue.setName("Venue " + suffix);
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(20);
        return venueRepository.saveAndFlush(venue);
    }

    private Event createEvent(
            String title, User organizer, Venue venue, int availableSeats) {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Organizer bookings integration test");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("25.00"));
        event.setTotalSeats(10);
        event.setAvailableSeats(availableSeats);
        event.setStatus(EventStatus.PUBLISHED);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        return eventRepository.saveAndFlush(event);
    }

    private Booking createBooking(
            User attendee, Event event, BookingStatus status,
            int seatsBooked, LocalDateTime bookingDate) {
        Booking booking = new Booking();
        booking.setUser(attendee);
        booking.setEvent(event);
        booking.setStatus(status);
        booking.setSeatsBooked(seatsBooked);
        booking.setBookingDate(bookingDate);
        return bookingRepository.saveAndFlush(booking);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
