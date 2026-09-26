package al.lhind.eventbooking.catalog;

import al.lhind.eventbooking.entity.Category;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.entity.Venue;
import al.lhind.eventbooking.repository.CategoryRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.repository.VenueRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogReadApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void visitorsCanReadCategoriesForEventFilters() throws Exception {
        Category category = new Category();
        category.setName("Music");
        Long id = categoryRepository.saveAndFlush(category).getId();

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].name").value("Music"));
    }

    @Test
    void onlyOrganizersCanReadVenueChoices() throws Exception {
        Venue venue = new Venue();
        venue.setName("Main Hall");
        venue.setAddress("1 Test Street");
        venue.setCity("Test City");
        venue.setCapacity(100);
        Long id = venueRepository.saveAndFlush(venue).getId();

        mockMvc.perform(get("/api/v1/organizer/venues"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/organizer/venues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(Role.ATTENDEE))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/organizer/venues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(Role.ORGANIZER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].name").value("Main Hall"));
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

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
