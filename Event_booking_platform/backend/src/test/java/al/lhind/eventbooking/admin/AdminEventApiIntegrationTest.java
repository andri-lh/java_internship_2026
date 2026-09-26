package al.lhind.eventbooking.admin;

import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminEventApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void adminListsEveryStatusAcrossOrganizersWithPaginationWhilePublicSeesPublishedOnly()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User firstOrganizer = createUser(Role.ORGANIZER);
        User secondOrganizer = createUser(Role.ORGANIZER);
        Venue venue = createVenue();
        Category category = createCategory();
        Event draft = createEvent("Draft event", EventStatus.DRAFT,
                firstOrganizer, venue, category);
        Event published = createEvent("Published event", EventStatus.PUBLISHED,
                secondOrganizer, venue, category);
        Event cancelled = createEvent("Cancelled event", EventStatus.CANCELLED,
                firstOrganizer, venue, category);

        mockMvc.perform(get("/api/v1/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(cancelled.getId()))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[0].organizerUsername")
                        .value(firstOrganizer.getUsername()))
                .andExpect(jsonPath("$.content[1].id").value(published.getId()))
                .andExpect(jsonPath("$.content[1].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.content[1].organizerUsername")
                        .value(secondOrganizer.getUsername()))
                .andExpect(jsonPath("$.content[1].venueName").value(venue.getName()))
                .andExpect(jsonPath("$.content[1].categories",
                        containsInAnyOrder(category.getName())));

        mockMvc.perform(get("/api/v1/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(draft.getId()))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(published.getId()));
    }

    @Test
    void adminCanViewDraftAndCancelledDetailsThatAreHiddenFromVisitors()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User organizer = createUser(Role.ORGANIZER);
        Venue venue = createVenue();
        Category category = createCategory();
        Event draft = createEvent("Private draft", EventStatus.DRAFT,
                organizer, venue, category);
        Event cancelled = createEvent("Removed event", EventStatus.CANCELLED,
                organizer, venue, category);

        mockMvc.perform(get("/api/v1/admin/events/{eventId}", draft.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(draft.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.venueName").value(venue.getName()))
                .andExpect(jsonPath("$.venueAddress").value(venue.getAddress()))
                .andExpect(jsonPath("$.city").value(venue.getCity()))
                .andExpect(jsonPath("$.organizerUsername")
                        .value(organizer.getUsername()))
                .andExpect(jsonPath("$.categories",
                        containsInAnyOrder(category.getName())));

        mockMvc.perform(get("/api/v1/admin/events/{eventId}", cancelled.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/events/{eventId}", draft.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/events/{eventId}", cancelled.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/admin/events/{eventId}", Long.MAX_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anonymousAttendeeAndOrganizerCannotUseAdminEventEndpoints()
            throws Exception {
        User organizer = createUser(Role.ORGANIZER);
        User attendee = createUser(Role.ATTENDEE);
        Event event = createEvent("Restricted event", EventStatus.DRAFT,
                organizer, createVenue(), createCategory());

        mockMvc.perform(get("/api/v1/admin/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(organizer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/events/{eventId}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isForbidden());
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

    private Venue createVenue() {
        Venue venue = new Venue();
        venue.setName("Venue " + UUID.randomUUID());
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(50);
        return venueRepository.saveAndFlush(venue);
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("Music " + UUID.randomUUID());
        return categoryRepository.saveAndFlush(category);
    }

    private Event createEvent(
            String title, EventStatus status, User organizer,
            Venue venue, Category category) {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Admin event integration test");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("25.00"));
        event.setTotalSeats(20);
        event.setAvailableSeats(20);
        event.setStatus(status);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setCategories(new HashSet<>(Set.of(category)));
        return eventRepository.saveAndFlush(event);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
