package al.lhind.eventbooking.admin;

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
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminBookingApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private WaitlistEntryRepository waitlistEntryRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void adminListsBookingsAcrossAllUsersAndOrganizersWithPagination()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizerOne = createUser(Role.ORGANIZER);
        User organizerTwo = createUser(Role.ORGANIZER);
        User attendeeOne = createUser(Role.ATTENDEE);
        User attendeeTwo = createUser(Role.ATTENDEE);
        Event firstEvent = createEvent(organizerOne, 5, 4,
                LocalDateTime.now().plusDays(5));
        Event secondEvent = createEvent(organizerTwo, 5, 3,
                LocalDateTime.now().plusDays(6));
        LocalDateTime bookedAt = LocalDateTime.now().withNano(0);

        Booking oldest = createBooking(attendeeOne, firstEvent,
                BookingStatus.CONFIRMED, 1, bookedAt.minusDays(2));
        Booking middle = createBooking(attendeeTwo, secondEvent,
                BookingStatus.CANCELLED, 1, bookedAt.minusDays(1));
        Booking newest = createBooking(attendeeOne, secondEvent,
                BookingStatus.CONFIRMED, 2, bookedAt);

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(newest.getId()))
                .andExpect(jsonPath("$.content[0].attendeeId")
                        .value(attendeeOne.getId()))
                .andExpect(jsonPath("$.content[0].attendeeUsername")
                        .value(attendeeOne.getUsername()))
                .andExpect(jsonPath("$.content[0].eventId")
                        .value(secondEvent.getId()))
                .andExpect(jsonPath("$.content[0].organizerId")
                        .value(organizerTwo.getId()))
                .andExpect(jsonPath("$.content[0].organizerUsername")
                        .value(organizerTwo.getUsername()))
                .andExpect(jsonPath("$.content[0].seatsBooked").value(2))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[1].id").value(middle.getId()))
                .andExpect(jsonPath("$.content[1].status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oldest.getId()))
                .andExpect(jsonPath("$.content[0].organizerId")
                        .value(organizerOne.getId()));
    }

    @Test
    void adminCanViewAnySingleBookingAndMissingIdReturnsNotFound()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizer = createUser(Role.ORGANIZER);
        User attendee = createUser(Role.ATTENDEE);
        Event event = createEvent(organizer, 5, 3,
                LocalDateTime.now().plusDays(5));
        Booking booking = createBooking(attendee, event,
                BookingStatus.CONFIRMED, 2, LocalDateTime.now());

        mockMvc.perform(get("/api/v1/admin/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.attendeeUsername")
                        .value(attendee.getUsername()))
                .andExpect(jsonPath("$.eventTitle").value(event.getTitle()))
                .andExpect(jsonPath("$.organizerUsername")
                        .value(organizer.getUsername()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/api/v1/admin/bookings/{bookingId}", Long.MAX_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCancelAnotherUsersBookingInsideAttendeeCancellationWindow()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizer = createUser(Role.ORGANIZER);
        User attendee = createUser(Role.ATTENDEE);
        Event event = createEvent(organizer, 5, 3,
                LocalDateTime.now().plusHours(12));
        Booking booking = createBooking(attendee, event,
                BookingStatus.CONFIRMED, 2, LocalDateTime.now());

        mockMvc.perform(delete("/api/v1/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/admin/bookings/{bookingId}/cancel",
                        booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.attendeeId").value(attendee.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(BookingStatus.CANCELLED,
                bookingRepository.findById(booking.getId()).orElseThrow().getStatus());
        assertEquals(5,
                eventRepository.findById(event.getId()).orElseThrow()
                        .getAvailableSeats());
    }

    @Test
    void adminCancellationPromotesWaitlistWithoutOverbooking() throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizer = createUser(Role.ORGANIZER);
        User bookedAttendee = createUser(Role.ATTENDEE);
        User firstWaiting = createUser(Role.ATTENDEE);
        User secondWaiting = createUser(Role.ATTENDEE);
        User thirdWaiting = createUser(Role.ATTENDEE);
        Event event = createEvent(organizer, 2, 0,
                LocalDateTime.now().plusDays(3));
        Booking original = createBooking(bookedAttendee, event,
                BookingStatus.CONFIRMED, 2, LocalDateTime.now().minusHours(2));
        LocalDateTime joinedAt = LocalDateTime.now().minusHours(1);
        WaitlistEntry first = createWaitingEntry(firstWaiting, event, joinedAt);
        WaitlistEntry second = createWaitingEntry(secondWaiting, event,
                joinedAt.plusMinutes(1));
        WaitlistEntry third = createWaitingEntry(thirdWaiting, event,
                joinedAt.plusMinutes(2));

        mockMvc.perform(patch("/api/v1/admin/bookings/{bookingId}/cancel",
                        original.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(BookingStatus.CANCELLED,
                bookingRepository.findById(original.getId()).orElseThrow().getStatus());
        assertEquals(WaitlistStatus.PROMOTED,
                waitlistEntryRepository.findById(first.getId()).orElseThrow().getStatus());
        assertEquals(WaitlistStatus.PROMOTED,
                waitlistEntryRepository.findById(second.getId()).orElseThrow().getStatus());
        assertEquals(WaitlistStatus.WAITING,
                waitlistEntryRepository.findById(third.getId()).orElseThrow().getStatus());

        List<Booking> active = bookingRepository.findByEventIdAndStatus(
                event.getId(), BookingStatus.CONFIRMED);
        assertEquals(2, active.size());
        assertEquals(2, active.stream().mapToInt(Booking::getSeatsBooked).sum());
        assertEquals(1, bookingRepository.findByUserIdAndStatus(
                firstWaiting.getId(), BookingStatus.CONFIRMED).size());
        assertEquals(1, bookingRepository.findByUserIdAndStatus(
                secondWaiting.getId(), BookingStatus.CONFIRMED).size());
        assertEquals(0,
                eventRepository.findById(event.getId()).orElseThrow()
                        .getAvailableSeats());

        mockMvc.perform(patch("/api/v1/admin/bookings/{bookingId}/cancel",
                        original.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isConflict());

        assertEquals(2, bookingRepository.findByEventIdAndStatus(
                event.getId(), BookingStatus.CONFIRMED).size());
        assertEquals(0,
                eventRepository.findById(event.getId()).orElseThrow()
                        .getAvailableSeats());
    }

    @Test
    void cancellingPastBookingDoesNotPromoteWaitingAttendee() throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizer = createUser(Role.ORGANIZER);
        User bookedAttendee = createUser(Role.ATTENDEE);
        User waitingAttendee = createUser(Role.ATTENDEE);
        Event event = createEvent(organizer, 1, 0,
                LocalDateTime.now().minusDays(2));
        Booking booking = createBooking(bookedAttendee, event,
                BookingStatus.CONFIRMED, 1, LocalDateTime.now().minusDays(3));
        WaitlistEntry waiting = createWaitingEntry(waitingAttendee, event,
                LocalDateTime.now().minusDays(3));

        mockMvc.perform(patch("/api/v1/admin/bookings/{bookingId}/cancel",
                        booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(1,
                eventRepository.findById(event.getId()).orElseThrow()
                        .getAvailableSeats());
        assertEquals(WaitlistStatus.WAITING,
                waitlistEntryRepository.findById(waiting.getId())
                        .orElseThrow().getStatus());
        assertFalse(bookingRepository.existsByEventIdAndStatus(
                event.getId(), BookingStatus.CONFIRMED));
    }

    @Test
    void onlyAdminsCanViewOrCancelBookings() throws Exception {
        User organizer = createUser(Role.ORGANIZER);
        User attendee = createUser(Role.ATTENDEE);
        Event event = createEvent(organizer, 1, 0,
                LocalDateTime.now().plusDays(3));
        Booking booking = createBooking(attendee, event,
                BookingStatus.CONFIRMED, 1, LocalDateTime.now());

        mockMvc.perform(get("/api/v1/admin/bookings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header(HttpHeaders.AUTHORIZATION, bearer(organizer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/bookings/{bookingId}", booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/admin/bookings/{bookingId}/cancel",
                        booking.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isForbidden());

        assertEquals(BookingStatus.CONFIRMED,
                bookingRepository.findById(booking.getId()).orElseThrow().getStatus());
        assertEquals(0,
                eventRepository.findById(event.getId()).orElseThrow()
                        .getAvailableSeats());

    }

    private User createUser(Role role) {
        String username = role.name().toLowerCase() + "-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("test-password");
        user.setRole(role);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }

    private Event createEvent(
            User organizer, int totalSeats, int availableSeats,
            LocalDateTime start) {
        Venue venue = new Venue();
        venue.setName("Venue " + UUID.randomUUID());
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(Math.max(10, totalSeats));
        venue = venueRepository.saveAndFlush(venue);

        Event event = new Event();
        event.setTitle("Event " + UUID.randomUUID());
        event.setDescription("Admin booking integration test");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("20.00"));
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(availableSeats);
        event.setStatus(EventStatus.PUBLISHED);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        return eventRepository.saveAndFlush(event);
    }

    private Booking createBooking(
            User attendee, Event event, BookingStatus status,
            int seats, LocalDateTime bookedAt) {
        Booking booking = new Booking();
        booking.setUser(attendee);
        booking.setEvent(event);
        booking.setSeatsBooked(seats);
        booking.setStatus(status);
        booking.setBookingDate(bookedAt);
        return bookingRepository.saveAndFlush(booking);
    }

    private WaitlistEntry createWaitingEntry(
            User attendee, Event event, LocalDateTime joinedAt) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(attendee);
        entry.setEvent(event);
        entry.setJoinedAt(joinedAt);
        entry.setStatus(WaitlistStatus.WAITING);
        return waitlistEntryRepository.saveAndFlush(entry);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
