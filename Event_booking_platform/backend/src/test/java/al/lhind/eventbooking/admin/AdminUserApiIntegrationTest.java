package al.lhind.eventbooking.admin;

import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserApiIntegrationTest extends MySqlIntegrationTest {

    private static final String USER_PASSWORD = "StrongPass123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenService jwtTokenService;

    @Test
    void adminCreatesOrganizerWithEncodedPasswordAndNoPasswordInResponse()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        String username = "new-organizer-" + UUID.randomUUID();
        String email = username + "@example.test";

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(username, email, USER_PASSWORD, "ORGANIZER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ORGANIZER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        User created = userRepository.findByUsername(username).orElseThrow();
        assertTrue(passwordEncoder.matches(USER_PASSWORD, created.getPassword()));
        assertFalse(USER_PASSWORD.equals(created.getPassword()));
        assertEquals(Role.ORGANIZER, created.getRole());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ORGANIZER"));
    }

    @Test
    void adminCanListViewAndUpdateAnAccount() throws Exception {
        User admin = createUser(Role.ADMIN);
        User attendee = createUser(Role.ATTENDEE);
        String originalToken = bearer(attendee);
        String newEmail = "promoted-" + UUID.randomUUID() + "@example.test";

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/admin/users/{userId}", attendee.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attendee.getId()))
                .andExpect(jsonPath("$.role").value("ATTENDEE"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(put("/api/v1/admin/users/{userId}", attendee.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(attendee.getUsername(), newEmail, "ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.role").value("ORGANIZER"));

        User updated = userRepository.findById(attendee.getId()).orElseThrow();
        assertEquals(newEmail, updated.getEmail());
        assertEquals(Role.ORGANIZER, updated.getRole());
        assertTrue(passwordEncoder.matches(USER_PASSWORD, updated.getPassword()));

        // The filter reloads the role from the database for an existing JWT.
        mockMvc.perform(get("/api/v1/organizer/bookings")
                        .header(HttpHeaders.AUTHORIZATION, originalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deactivationBlocksLoginAndExistingTokenUntilReactivation()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User attendee = createUser(Role.ATTENDEE);
        String attendeeToken = bearer(attendee);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/activation", attendee.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertFalse(userRepository.findById(attendee.getId()).orElseThrow().isActive());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(attendee.getUsername(), USER_PASSWORD)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION, attendeeToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/activation", attendee.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(attendee.getUsername(), USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void adminCannotDeactivateOrDemoteOwnAccount() throws Exception {
        User admin = createUser(Role.ADMIN);
        String token = bearer(admin);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/activation", admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active": false}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/admin/users/{userId}", admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(
                                admin.getUsername(), admin.getEmail(), "ATTENDEE")))
                .andExpect(status().isConflict());

        User unchanged = userRepository.findById(admin.getId()).orElseThrow();
        assertTrue(unchanged.isActive());
        assertEquals(Role.ADMIN, unchanged.getRole());
    }

    @Test
    void duplicateUsernameOrEmailIsRejectedOnCreateAndUpdate()
            throws Exception {
        User admin = createUser(Role.ADMIN);
        User existing = createUser(Role.ATTENDEE);
        User other = createUser(Role.ORGANIZER);
        long initialCount = userRepository.count();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(existing.getUsername().toUpperCase(),
                                "unique-" + UUID.randomUUID() + "@example.test",
                                USER_PASSWORD, "ATTENDEE")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("unique-" + UUID.randomUUID(),
                                existing.getEmail().toUpperCase(),
                                USER_PASSWORD, "ATTENDEE")))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/admin/users/{userId}", other.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson(existing.getUsername().toUpperCase(),
                                other.getEmail(), "ORGANIZER")))
                .andExpect(status().isConflict());

        assertEquals(initialCount, userRepository.count());
        assertEquals(other.getUsername(),
                userRepository.findById(other.getId()).orElseThrow().getUsername());
    }

    @Test
    void invalidInputAndMissingAccountsReturnClientErrors() throws Exception {
        User admin = createUser(Role.ADMIN);
        String token = bearer(admin);

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("ab", "not-an-email", "short", "ATTENDEE")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/activation", admin.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/admin/users/{userId}", Long.MAX_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());

        assertEquals(1, userRepository.count());
    }

    @Test
    void onlyAdminsCanUseUserManagementEndpoints() throws Exception {
        User attendee = createUser(Role.ATTENDEE);
        User organizer = createUser(Role.ORGANIZER);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attendee)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(organizer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("forbidden-user", "forbidden@example.test",
                                USER_PASSWORD, "ATTENDEE")))
                .andExpect(status().isForbidden());

        assertEquals(2, userRepository.count());
    }

    private User createUser(Role role) {
        String username = role.name().toLowerCase() + "-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        user.setRole(role);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }

    private String createJson(
            String username, String email, String password, String role) {
        return """
                {
                  "username": "%s",
                  "email": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(username, email, password, role);
    }

    private String updateJson(String username, String email, String role) {
        return """
                {
                  "username": "%s",
                  "email": "%s",
                  "role": "%s"
                }
                """.formatted(username, email, role);
    }

    private String loginJson(String username, String password) {
        return """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);
    }
}
