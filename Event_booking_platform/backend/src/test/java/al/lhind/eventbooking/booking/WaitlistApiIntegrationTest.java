package al.lhind.eventbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.entity.WaitlistStatus;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

class WaitlistApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private WaitlistEntryRepository waitlistEntryRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void attendeeJoinsListsAndLeavesTheWaitlistOfASoldOutEvent() throws Exception {
        Event event = soldOutEvent();
        User alice = user("alice", Role.ATTENDEE);

        mockMvc.perform(post("/api/v1/events/{id}/waitlist", event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/waitlist").header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(event.getId()))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        Long entryId = waitlistEntryRepository.findByUserIdAndEventId(alice.getId(), event.getId()).orElseThrow().getId();

        mockMvc.perform(delete("/api/v1/waitlist/{id}", entryId).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        assertEquals(WaitlistStatus.CANCELLED, waitlistEntryRepository.findById(entryId).orElseThrow().getStatus());

        mockMvc.perform(delete("/api/v1/waitlist/{id}", entryId).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isConflict());
    }

    @Test
    void anAttendeeCannotSeeOrLeaveSomeoneElsesEntry() throws Exception {
        Event event = soldOutEvent();
        User alice = user("alice", Role.ATTENDEE);
        User bob = user("bob", Role.ATTENDEE);

        mockMvc.perform(post("/api/v1/events/{id}/waitlist", event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(alice)))
                .andExpect(status().isCreated());
        Long entryId = waitlistEntryRepository.findByUserIdAndEventId(alice.getId(), event.getId()).orElseThrow().getId();

        mockMvc.perform(get("/api/v1/waitlist").header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(delete("/api/v1/waitlist/{id}", entryId).header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyAttendeesCanUseTheWaitlistEndpoints() throws Exception {
        User organizer = user("organizer", Role.ORGANIZER);

        mockMvc.perform(get("/api/v1/waitlist")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/waitlist").header(HttpHeaders.AUTHORIZATION, bearer(organizer)))
                .andExpect(status().isForbidden());
    }

    private Event soldOutEvent() {
        User organizer = user("organizer-" + System.nanoTime(), Role.ORGANIZER);
        Venue venue = new Venue();
        venue.setName("Venue");
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(10);
        venue = venueRepository.saveAndFlush(venue);

        Event event = new Event();
        event.setTitle("Sold out");
        event.setDescription("Waitlist test");
        event.setStartDateTime(LocalDateTime.now().plusDays(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(5).plusHours(2));
        event.setPrice(BigDecimal.TEN);
        event.setTotalSeats(2);
        event.setAvailableSeats(0);
        event.setStatus(EventStatus.PUBLISHED);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        return eventRepository.saveAndFlush(event);
    }

    private User user(String username, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@example.test");
            user.setPassword("test-password");
            user.setRole(role);
            user.setActive(true);
            return userRepository.saveAndFlush(user);
        });
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
