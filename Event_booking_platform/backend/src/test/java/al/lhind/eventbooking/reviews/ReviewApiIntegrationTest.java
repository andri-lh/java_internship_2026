package al.lhind.eventbooking.reviews;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
import al.lhind.eventbooking.entity.Booking;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.entity.EventStatus;
import al.lhind.eventbooking.entity.Review;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewApiIntegrationTest extends MySqlIntegrationTest {

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
    private ReviewRepository reviewRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void attendeeWithConfirmedBookingCanReviewAnEventAfterItEnds()
            throws Exception {
        TestData data = createFixture(
                LocalDateTime.now().minusHours(1),
                true
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Excellent event!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId")
                        .value(data.event().getId().intValue()))
                .andExpect(jsonPath("$.username")
                        .value(data.attendee().getUsername()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Excellent event!"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertEquals(1L, reviewRepository.count());

        Review savedReview = reviewRepository.findAll().getFirst();
        assertEquals(data.attendee().getId(), savedReview.getUser().getId());
        assertEquals(data.event().getId(), savedReview.getEvent().getId());
        assertEquals(5, savedReview.getRating());
    }

    @Test
    void attendeeWithoutConfirmedBookingCannotReviewEvent()
            throws Exception {
        TestData data = createFixture(
                LocalDateTime.now().minusHours(1),
                false
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Nice event"
                                }
                                """))
                .andExpect(status().isForbidden());

        assertEquals(0L, reviewRepository.count());
    }

    @Test
    void attendeeCannotReviewAnEventBeforeItEnds() throws Exception {
        TestData data = createFixture(
                LocalDateTime.now().plusDays(1),
                true
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Looking forward to it"
                                }
                                """))
                .andExpect(status().isConflict());

        assertEquals(0L, reviewRepository.count());
    }

    @Test
    void attendeeCanLeaveAtMostOneReviewForAnEvent() throws Exception {
        TestData data = createFixture(
                LocalDateTime.now().minusHours(1),
                true
        );
        String token = jwtTokenService.generateToken(data.attendee());
        String requestBody = """
                {
                  "rating": 5,
                  "comment": "Excellent event!"
                }
                """;

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());

        assertEquals(1L, reviewRepository.count());
    }

    @Test
    void ratingOutsideOneToFiveIsRejected() throws Exception {
        TestData data = createFixture(
                LocalDateTime.now().minusHours(1),
                true
        );
        String token = jwtTokenService.generateToken(data.attendee());

        mockMvc.perform(post(
                        "/api/v1/events/{eventId}/reviews",
                        data.event().getId())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 6,
                                  "comment": "Invalid rating"
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(0L, reviewRepository.count());
    }

    private TestData createFixture(
            LocalDateTime eventEnd,
            boolean createConfirmedBooking) {
        String suffix = UUID.randomUUID().toString();

        User organizer = createUser(
                "organizer-" + suffix,
                Role.ORGANIZER
        );
        User attendee = createUser(
                "attendee-" + suffix,
                Role.ATTENDEE
        );

        Venue venue = new Venue();
        venue.setName("Test Venue " + suffix);
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(20);
        venue = venueRepository.saveAndFlush(venue);

        Event event = new Event();
        event.setTitle("Test Event " + suffix);
        event.setDescription("Review integration test event");
        event.setStartDateTime(eventEnd.minusHours(2));
        event.setEndDateTime(eventEnd);
        event.setPrice(new BigDecimal("25.00"));
        event.setTotalSeats(20);
        event.setAvailableSeats(createConfirmedBooking ? 19 : 20);
        event.setStatus(EventStatus.PUBLISHED);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event = eventRepository.saveAndFlush(event);

        if (createConfirmedBooking) {
            Booking booking = new Booking();
            booking.setUser(attendee);
            booking.setEvent(event);
            booking.setSeatsBooked(1);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setBookingDate(LocalDateTime.now());
            bookingRepository.saveAndFlush(booking);
        }

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

    private record TestData(User attendee, Event event) {
    }
}
