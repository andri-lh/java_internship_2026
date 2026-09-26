package al.lhind.eventbooking.auth;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void validRegistrationCreatesActiveAttendeeWithEncodedPassword()
            throws Exception {
        String suffix = UUID.randomUUID().toString();
        String username = "attendee-" + suffix;
        String email = "attendee-" + suffix + "@example.test";
        String rawPassword = "Safe-password-123";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, email, rawPassword)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ATTENDEE"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository.findByUsername(username)
                .orElseThrow();

        assertEquals(email, savedUser.getEmail());
        assertEquals(Role.ATTENDEE, savedUser.getRole());
        assertTrue(savedUser.isActive());
        assertNotEquals(rawPassword, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    void invalidRegistrationIsRejectedAndDoesNotCreateAUser() throws Exception {
        String email = "invalid-" + UUID.randomUUID() + "@example.test";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "%s",
                                  "password": "short"
                                }
                                """.formatted(email)))
                .andExpect(status().isBadRequest());

        Optional<User> savedUser = userRepository.findByEmail(email);
        assertFalse(savedUser.isPresent());
    }
}
