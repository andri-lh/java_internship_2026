package al.lhind.eventbooking.auth;

import al.lhind.eventbooking.support.MySqlIntegrationTest;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginApiIntegrationTest extends MySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void validCredentialsReturnBearerJwtForStoredUser() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String username = "login-user-" + suffix;
        String rawPassword = "correct-password-123";
        User user = createActiveAttendee(username, rawPassword);

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, rawPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = responseBody.replaceFirst(
                "^.*\\\"accessToken\\\"\\s*:\\s*\\\"([^\\\"]+)\\\".*$",
                "$1"
        );

        assertEquals(username, jwtTokenService.extractClaims(token).getSubject());
        assertEquals(user.getId(), jwtTokenService.extractClaims(token)
                .get("userId", Number.class).longValue());
    }

    @Test
    void incorrectPasswordReturnsUnauthorized() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String username = "login-user-" + suffix;
        createActiveAttendee(username, "correct-password-123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "incorrect-password"
                                }
                                """.formatted(username)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidLoginRequestIsRejectedByValidation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private User createActiveAttendee(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.ATTENDEE);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }
}
