package al.lhind.eventbooking.events;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Review;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventDetailsApiIntegrationTest extends MySqlIntegrationTest {

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

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void publishedEventDetailsIncludeVenueOrganizerCategoriesAndAverageRating()
            throws Exception {
        TestData data = createFixture(EventStatus.PUBLISHED, true);

        mockMvc.perform(get("/api/v1/events/{eventId}", data.event().getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(data.event().getId()))
                .andExpect(jsonPath("$.title").value(data.event().getTitle()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.venueName").value(data.venue().getName()))
                .andExpect(jsonPath("$.venueAddress").value(data.venue().getAddress()))
                .andExpect(jsonPath("$.city").value(data.venue().getCity()))
                .andExpect(jsonPath("$.organizerUsername")
                        .value(data.organizer().getUsername()))
                .andExpect(jsonPath("$.categories", containsInAnyOrder(
                        data.music().getName(),
                        data.arts().getName()
                )))
                .andExpect(jsonPath("$.averageRating").value(4.0));
    }

    @Test
    void draftEventDetailsAreNotPublic() throws Exception {
        TestData data = createFixture(EventStatus.DRAFT, false);

        mockMvc.perform(get("/api/v1/events/{eventId}", data.event().getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private TestData createFixture(EventStatus status, boolean addReviews) {
        String suffix = UUID.randomUUID().toString();

        User organizer = createUser("organizer-" + suffix, Role.ORGANIZER);
        Category music = createCategory("Music-" + suffix);
        Category arts = createCategory("Arts-" + suffix);

        Venue venue = new Venue();
        venue.setName("Test Venue " + suffix);
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(50);
        venue = venueRepository.saveAndFlush(venue);

        LocalDateTime start = LocalDateTime.now().plusDays(10);
        Event event = new Event();
        event.setTitle("Test Event " + suffix);
        event.setDescription("Event details integration test");
        event.setStartDateTime(start);
        event.setEndDateTime(start.plusHours(2));
        event.setPrice(new BigDecimal("25.00"));
        event.setTotalSeats(50);
        event.setAvailableSeats(50);
        event.setStatus(status);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setCategories(Set.of(music, arts));
        event = eventRepository.saveAndFlush(event);

        if (addReviews) {
            User firstReviewer = createUser("reviewer-one-" + suffix, Role.ATTENDEE);
            User secondReviewer = createUser("reviewer-two-" + suffix, Role.ATTENDEE);
            saveReview(firstReviewer, event, 5);
            saveReview(secondReviewer, event, 3);
        }

        return new TestData(event, venue, organizer, music, arts);
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("test-password");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }

    private void saveReview(User user, Event event, int rating) {
        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(rating);
        review.setComment("Integration test review");
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.saveAndFlush(review);
    }

    private record TestData(
            Event event,
            Venue venue,
            User organizer,
            Category music,
            Category arts
    ) {
    }
}
