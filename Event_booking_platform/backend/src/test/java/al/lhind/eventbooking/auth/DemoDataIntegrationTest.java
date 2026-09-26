package al.lhind.eventbooking.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import al.lhind.eventbooking.config.DemoDataRunner;
import al.lhind.eventbooking.entity.BookingStatus;
import al.lhind.eventbooking.entity.Event;
import al.lhind.eventbooking.repository.BookingRepository;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.EventRepository;
import al.lhind.eventbooking.repository.ReviewRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.repository.WaitlistEntryRepository;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DemoDataIntegrationTest extends MySqlIntegrationTest {

    private static final String PASSWORD = "Demo#Pass2026";

    @Autowired private UserRepository userRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private WaitlistEntryRepository waitlistEntryRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mockMvc;

    @Test
    void createsConsistentDemoContentThatUsersCanSignInToAndIsRepeatSafe() throws Exception {
        runner(PASSWORD).seed();

        assertEquals(5, userRepository.count());
        assertEquals(5, venueRepository.count());
        assertEquals(10, eventRepository.count());
        assertEquals(9, bookingRepository.count());
        assertEquals(1, waitlistEntryRepository.count());
        assertEquals(3, reviewRepository.count());

        for (Event event : eventRepository.findAll()) {
            int confirmed = bookingRepository.findByEventIdAndStatus(event.getId(), BookingStatus.CONFIRMED).stream()
                    .mapToInt(booking -> booking.getSeatsBooked())
                    .sum();
            assertEquals(event.getTotalSeats() - confirmed, event.getAvailableSeats(), event.getTitle());
            assertTrue(event.getImageUrl().startsWith("https://"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo_alice\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/events?size=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7));

        runner(PASSWORD).seed();
        assertEquals(5, userRepository.count());
        assertEquals(10, eventRepository.count());
        assertEquals(8, categoryRepository.count());
    }

    @Test
    void refusesToRunWithAWeakPassword() {
        runner("weak").seed();

        assertEquals(0, userRepository.count());
        assertEquals(0, eventRepository.count());
    }

    private DemoDataRunner runner(String password) {
        return new DemoDataRunner(userRepository, venueRepository, categoryRepository, eventRepository,
                bookingRepository, waitlistEntryRepository, reviewRepository, passwordEncoder, password);
    }
}
