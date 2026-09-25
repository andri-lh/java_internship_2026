package al.lhind.eventbooking.events;

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
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizerEventApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private WaitlistEntryRepository waitlistEntryRepository;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void organizerCreatesDraftWithVenueCategoriesAndFullAvailability() throws Exception {
        Fixture fixture = fixture();
        LocalDateTime start = futureStart();

        mockMvc.perform(post("/api/v1/organizer/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Concert", "25.00", start,
                                start.plusHours(2), 4, fixture)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Concert"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalSeats").value(4))
                .andExpect(jsonPath("$.availableSeats").value(4))
                .andExpect(jsonPath("$.venueId").value(fixture.venue().getId()))
                .andExpect(jsonPath("$.categoryIds", containsInAnyOrder(
                        fixture.category().getId().intValue())));

        Event saved = onlyEventOf(fixture.organizer());
        assertEquals(fixture.organizer().getId(), saved.getOrganizer().getId());
        assertEquals(fixture.venue().getId(), saved.getVenue().getId());
        assertEquals(EventStatus.DRAFT, saved.getStatus());
        assertEquals(4, saved.getAvailableSeats());
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from event_categories where event_id = ?",
                Integer.class, saved.getId()));

        mockMvc.perform(get("/api/v1/events/{eventId}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerUpdatesDraftAndPublishesIt() throws Exception {
        Fixture fixture = fixture();
        Event created = createDraft(fixture);
        LocalDateTime newStart = futureStart().plusDays(1);

        mockMvc.perform(put("/api/v1/organizer/events/{eventId}", created.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Updated concert", "35.00", newStart,
                                newStart.plusHours(3), 6, fixture)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated concert"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.availableSeats").value(6));

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/publish", created.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        Event saved = eventRepository.findById(created.getId()).orElseThrow();
        assertEquals("Updated concert", saved.getTitle());
        assertEquals(new BigDecimal("35.00"), saved.getPrice());
        assertEquals(6, saved.getTotalSeats());
        assertEquals(6, saved.getAvailableSeats());
        assertEquals(EventStatus.PUBLISHED, saved.getStatus());

        mockMvc.perform(get("/api/v1/events/{eventId}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated concert"));
    }

    @Test
    void anotherOrganizerCannotUpdatePublishOrCancelAnEvent() throws Exception {
        Fixture fixture = fixture();
        Event event = createDraft(fixture);
        User other = createUser("other-organizer", Role.ORGANIZER);
        LocalDateTime start = futureStart();

        mockMvc.perform(put("/api/v1/organizer/events/{eventId}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Stolen title", "25.00", start,
                                start.plusHours(2), 4, fixture)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/publish", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(other)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/cancel", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(other)))
                .andExpect(status().isNotFound());

        Event unchanged = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals("Concert", unchanged.getTitle());
        assertEquals(EventStatus.DRAFT, unchanged.getStatus());
        assertEquals(fixture.organizer().getId(), unchanged.getOrganizer().getId());
    }

    @Test
    void attendeeAndAnonymousVisitorCannotCreateOrganizerEvents() throws Exception {
        Fixture fixture = fixture();
        User attendee = createUser("attendee", Role.ATTENDEE);
        LocalDateTime start = futureStart();
        String body = eventJson("Concert", "25.00", start,
                start.plusHours(2), 4, fixture);

        mockMvc.perform(post("/api/v1/organizer/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/organizer/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        assertTrue(eventRepository.findAll().isEmpty());
    }

    @Test
    void invalidScheduleAndExcessCapacityDoNotCreateEvents() throws Exception {
        Fixture fixture = fixture();
        LocalDateTime start = futureStart();

        mockMvc.perform(post("/api/v1/organizer/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Bad dates", "25.00", start,
                                start.minusHours(1), 4, fixture)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/organizer/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Too many seats", "25.00", start,
                                start.plusHours(2), 11, fixture)))
                .andExpect(status().isBadRequest());

        assertTrue(eventRepository.findAll().isEmpty());
    }

    @Test
    void publishedEventWithConfirmedBookingCannotBeUpdated() throws Exception {
        Fixture fixture = fixture();
        Event event = createDraft(fixture);
        publish(event, fixture.organizer());
        Event published = eventRepository.findById(event.getId()).orElseThrow();
        User attendee = createUser("booker", Role.ATTENDEE);
        createBooking(attendee, published, 1);
        published.setAvailableSeats(3);
        eventRepository.saveAndFlush(published);
        LocalDateTime newStart = futureStart().plusDays(1);

        mockMvc.perform(put("/api/v1/organizer/events/{eventId}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Changed after booking", "40.00", newStart,
                                newStart.plusHours(2), 4, fixture)))
                .andExpect(status().isConflict());

        Event unchanged = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals("Concert", unchanged.getTitle());
        assertEquals(EventStatus.PUBLISHED, unchanged.getStatus());
        assertEquals(3, unchanged.getAvailableSeats());
    }

    @Test
    void cancellingPublishedEventCancelsBookingsAndWaitlistAndHidesEvent() throws Exception {
        Fixture fixture = fixture();
        Event event = createDraft(fixture);
        publish(event, fixture.organizer());
        Event published = eventRepository.findById(event.getId()).orElseThrow();
        User booker = createUser("booker", Role.ATTENDEE);
        User waiting = createUser("waiting", Role.ATTENDEE);
        Booking booking = createBooking(booker, published, 4);
        WaitlistEntry entry = createWaitingEntry(waiting, published);
        published.setAvailableSeats(0);
        eventRepository.saveAndFlush(published);

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/cancel", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.availableSeats").value(4));

        Event cancelled = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(EventStatus.CANCELLED, cancelled.getStatus());
        assertEquals(4, cancelled.getAvailableSeats());
        assertEquals(BookingStatus.CANCELLED,
                bookingRepository.findById(booking.getId()).orElseThrow().getStatus());
        assertEquals(WaitlistStatus.CANCELLED,
                waitlistEntryRepository.findById(entry.getId()).orElseThrow().getStatus());
        assertFalse(bookingRepository.existsByEventIdAndStatus(
                event.getId(), BookingStatus.CONFIRMED));

        mockMvc.perform(get("/api/v1/events/{eventId}", event.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishedEventCannotBePublishedAgainAndCancelledEventCannotBeUpdated()
            throws Exception {
        Fixture fixture = fixture();
        Event event = createDraft(fixture);
        publish(event, fixture.organizer());

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/publish", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer())))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/cancel", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer())))
                .andExpect(status().isOk());

        LocalDateTime start = futureStart();
        mockMvc.perform(put("/api/v1/organizer/events/{eventId}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("New title", "25.00", start,
                                start.plusHours(2), 4, fixture)))
                .andExpect(status().isConflict());

        assertEquals(EventStatus.CANCELLED,
                eventRepository.findById(event.getId()).orElseThrow().getStatus());
    }

    private Fixture fixture() {
        String suffix = UUID.randomUUID().toString();
        User organizer = createUser("organizer-" + suffix, Role.ORGANIZER);

        Venue venue = new Venue();
        venue.setName("Venue " + suffix);
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(10);
        venue = venueRepository.saveAndFlush(venue);

        Category category = new Category();
        category.setName("Music " + suffix);
        category = categoryRepository.saveAndFlush(category);
        return new Fixture(organizer, venue, category);
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

    private Event createDraft(Fixture fixture) throws Exception {
        LocalDateTime start = futureStart();
        mockMvc.perform(post("/api/v1/organizer/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.organizer()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Concert", "25.00", start,
                                start.plusHours(2), 4, fixture)))
                .andExpect(status().isCreated());
        return onlyEventOf(fixture.organizer());
    }

    private Event onlyEventOf(User organizer) {
        var events = eventRepository.findByOrganizerId(organizer.getId());
        assertEquals(1, events.size());
        return events.getFirst();
    }

    private void publish(Event event, User organizer) throws Exception {
        mockMvc.perform(patch("/api/v1/organizer/events/{eventId}/publish", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(organizer)))
                .andExpect(status().isOk());
    }

    private Booking createBooking(User user, Event event, int seats) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepository.saveAndFlush(booking);
    }

    private WaitlistEntry createWaitingEntry(User user, Event event) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(user);
        entry.setEvent(event);
        entry.setStatus(WaitlistStatus.WAITING);
        entry.setJoinedAt(LocalDateTime.now());
        return waitlistEntryRepository.saveAndFlush(entry);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }

    private LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(5).withNano(0);
    }

    private String eventJson(
            String title, String price, LocalDateTime start,
            LocalDateTime end, int seats, Fixture fixture) {
        return """
                {
                  "title": "%s",
                  "description": "Integration test event",
                  "startDateTime": "%s",
                  "endDateTime": "%s",
                  "price": %s,
                  "totalSeats": %d,
                  "venueId": %d,
                  "categoryIds": [%d]
                }
                """.formatted(title, start, end, price, seats,
                fixture.venue().getId(), fixture.category().getId());
    }

    private record Fixture(User organizer, Venue venue, Category category) {}
}
