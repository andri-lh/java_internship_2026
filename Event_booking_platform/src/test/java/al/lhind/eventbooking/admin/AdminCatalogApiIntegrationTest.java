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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCatalogApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void adminCanCreateReadUpdateAndDeleteVenue() throws Exception {
        String token = bearer(createUser(Role.ADMIN));

        mockMvc.perform(post("/api/v1/admin/venues")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("Old Hall", 50)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Old Hall"))
                .andExpect(jsonPath("$.address").value("1 Test Street"))
                .andExpect(jsonPath("$.city").value("Test City"))
                .andExpect(jsonPath("$.capacity").value(50));

        Venue created = venueRepository.findAll().getFirst();
        Long venueId = created.getId();

        mockMvc.perform(get("/api/v1/admin/venues/{venueId}", venueId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venueId));

        mockMvc.perform(get("/api/v1/admin/venues")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(put("/api/v1/admin/venues/{venueId}", venueId)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("New Hall", 60)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Hall"))
                .andExpect(jsonPath("$.capacity").value(60));

        Venue updated = venueRepository.findById(venueId).orElseThrow();
        assertEquals("New Hall", updated.getName());
        assertEquals(60, updated.getCapacity());

        mockMvc.perform(delete("/api/v1/admin/venues/{venueId}", venueId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        assertFalse(venueRepository.existsById(venueId));
        mockMvc.perform(get("/api/v1/admin/venues/{venueId}", venueId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCreateReadUpdateAndDeleteCategory() throws Exception {
        String token = bearer(createUser(Role.ADMIN));

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Music")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Music"));

        Category created = categoryRepository.findAll().getFirst();
        Long categoryId = created.getId();

        mockMvc.perform(get("/api/v1/admin/categories/{categoryId}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId));

        mockMvc.perform(get("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Theatre")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Theatre"));

        assertEquals("Theatre",
                categoryRepository.findById(categoryId).orElseThrow().getName());

        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.existsById(categoryId));
        mockMvc.perform(get("/api/v1/admin/categories/{categoryId}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void venueInUseCannotBeDeletedOrReducedBelowEventSeatCount() throws Exception {
        String token = bearer(createUser(Role.ADMIN));
        User organizer = createUser(Role.ORGANIZER);
        Venue venue = createVenue(50);
        Event event = createEvent(organizer, venue, null, 30);

        mockMvc.perform(put("/api/v1/admin/venues/{venueId}", venue.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("Too Small", 20)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/admin/venues/{venueId}", venue.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict());

        Venue unchanged = venueRepository.findById(venue.getId()).orElseThrow();
        assertEquals(50, unchanged.getCapacity());
        assertEquals("Test Venue", unchanged.getName());
        assertTrue(eventRepository.existsById(event.getId()));

        mockMvc.perform(put("/api/v1/admin/venues/{venueId}", venue.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("Exact Capacity", 30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(30));
    }

    @Test
    void categoryUsedByAnEventCannotBeDeleted() throws Exception {
        String token = bearer(createUser(Role.ADMIN));
        User organizer = createUser(Role.ORGANIZER);
        Venue venue = createVenue(50);
        Category category = createCategory("Music");
        Event event = createEvent(organizer, venue, category, 30);

        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}",
                        category.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict());

        assertTrue(categoryRepository.existsById(category.getId()));
        assertTrue(eventRepository.existsById(event.getId()));
    }

    @Test
    void categoryNamesAreUniqueIgnoringCaseOnCreateAndUpdate() throws Exception {
        String token = bearer(createUser(Role.ADMIN));
        Category music = createCategory("Music");
        Category theatre = createCategory("Theatre");

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(" music ")))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}",
                        theatre.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("MUSIC")))
                .andExpect(status().isConflict());

        assertEquals(2, categoryRepository.count());
        assertEquals("Music",
                categoryRepository.findById(music.getId()).orElseThrow().getName());
        assertEquals("Theatre",
                categoryRepository.findById(theatre.getId()).orElseThrow().getName());
    }

    @Test
    void invalidRequestsAreRejectedBeforePersistence() throws Exception {
        String token = bearer(createUser(Role.ADMIN));

        mockMvc.perform(post("/api/v1/admin/venues")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("Test Venue", 0)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("   ")))
                .andExpect(status().isBadRequest());

        assertEquals(0, venueRepository.count());
        assertEquals(0, categoryRepository.count());
    }

    @Test
    void onlyAdminsCanManageVenuesAndCategories() throws Exception {
        User organizer = createUser(Role.ORGANIZER);
        User attendee = createUser(Role.ATTENDEE);

        mockMvc.perform(get("/api/v1/admin/venues"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/venues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(organizer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(venueJson("Forbidden Venue", 50)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Forbidden Category")))
                .andExpect(status().isForbidden());

        assertEquals(0, venueRepository.count());
        assertEquals(0, categoryRepository.count());
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

    private Venue createVenue(int capacity) {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(capacity);
        return venueRepository.saveAndFlush(venue);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }

    private Event createEvent(
            User organizer, Venue venue, Category category, int totalSeats) {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        Event event = new Event();
        event.setTitle("Event using admin catalog data");
        event.setDescription("Integration test event");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("10.00"));
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(totalSeats);
        event.setStatus(EventStatus.DRAFT);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        if (category != null) {
            event.setCategories(new HashSet<>(Set.of(category)));
        }
        return eventRepository.saveAndFlush(event);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }

    private String venueJson(String name, int capacity) {
        return """
                {
                  "name": "%s",
                  "address": "1 Test Street",
                  "city": "Test City",
                  "capacity": %d
                }
                """.formatted(name, capacity);
    }

    private String categoryJson(String name) {
        return """
                {"name": "%s"}
                """.formatted(name);
    }
}
