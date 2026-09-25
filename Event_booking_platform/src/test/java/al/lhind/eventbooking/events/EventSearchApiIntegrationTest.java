package al.lhind.eventbooking.events;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventSearchApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void filtersPublishedEventsByCityCategoryDateAndPrice() throws Exception {
        TestData data = createSearchFixtures();

        mockMvc.perform(get("/api/v1/events")
                        .param("city", "bErLiN")
                        .param("startsAfter", data.base().plusDays(2).toString())
                        .param("startsBefore", data.base().plusDays(4).toString())
                        .param("minimumPrice", "20.00")
                        .param("maximumPrice", "30.00")
                        .param("categoryId", data.music().getId().toString())
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value(data.alpha().getTitle()))
                .andExpect(jsonPath("$.content[1].title").value(data.beta().getTitle()));
    }

    @Test
    void paginatesAndSortsPublishedEvents() throws Exception {
        TestData data = createSearchFixtures();

        mockMvc.perform(get("/api/v1/events")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "startDateTime,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value(data.delta().getTitle()))
                .andExpect(jsonPath("$.content[1].title").value(data.beta().getTitle()));

        mockMvc.perform(get("/api/v1/events")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "startDateTime,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content[0].title").value(data.gamma().getTitle()))
                .andExpect(jsonPath("$.content[1].title").value(data.alpha().getTitle()));
    }

    @Test
    void invalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("startsAfter", "2035-06-10T10:00:00")
                        .param("startsBefore", "2035-06-09T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPriceRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("minimumPrice", "50.00")
                        .param("maximumPrice", "20.00"))
                .andExpect(status().isBadRequest());
    }

    private TestData createSearchFixtures() {
        String suffix = UUID.randomUUID().toString();

        User organizer = new User();
        organizer.setUsername("organizer-" + suffix);
        organizer.setEmail("organizer-" + suffix + "@example.test");
        organizer.setPassword("test-password");
        organizer.setRole(Role.ORGANIZER);
        organizer = userRepository.saveAndFlush(organizer);

        Category music = createCategory("Music-" + suffix);
        Category technology = createCategory("Technology-" + suffix);
        LocalDateTime base = LocalDateTime.now().plusDays(10).withNano(0);

        Event alpha = createEvent(
                "Alpha-" + suffix, "Berlin", base.plusDays(2), "20.00",
                EventStatus.PUBLISHED, organizer, Set.of(music, technology));
        Event beta = createEvent(
                "Beta-" + suffix, "Berlin", base.plusDays(4), "30.00",
                EventStatus.PUBLISHED, organizer, Set.of(music));
        Event gamma = createEvent(
                "Gamma-" + suffix, "Berlin", base.plusDays(3), "25.00",
                EventStatus.PUBLISHED, organizer, Set.of(technology));
        Event delta = createEvent(
                "Delta-" + suffix, "Munich", base.plusDays(5), "25.00",
                EventStatus.PUBLISHED, organizer, Set.of(music));
        createEvent(
                "Draft-" + suffix, "Berlin", base.plusDays(3), "28.00",
                EventStatus.DRAFT, organizer, Set.of(music));

        return new TestData(base, music, alpha, beta, gamma, delta);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }

    private Event createEvent(
            String title,
            String city,
            LocalDateTime start,
            String price,
            EventStatus status,
            User organizer,
            Set<Category> categories) {
        Venue venue = new Venue();
        venue.setName("Venue-" + title);
        venue.setAddress("1 Test Street");
        venue.setCity(city);
        venue.setCapacity(100);
        venue = venueRepository.saveAndFlush(venue);

        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Integration test event");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal(price));
        event.setTotalSeats(100);
        event.setAvailableSeats(100);
        event.setStatus(status);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setCategories(categories);
        return eventRepository.saveAndFlush(event);
    }

    private record TestData(
            LocalDateTime base,
            Category music,
            Event alpha,
            Event beta,
            Event gamma,
            Event delta
    ) {
    }
}
