package al.lhind.eventbooking.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import al.lhind.eventbooking.entity.PasswordResetToken;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.PasswordResetTokenRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.AccountMailSender;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class PasswordResetApiIntegrationTest extends MySqlIntegrationTest {

    private static final String OLD_PASSWORD = "OldPassword1!";
    private static final String NEW_PASSWORD = "NewPassword2@";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private AccountMailSender mailSender;

    @Test
    void knownAndUnknownEmailsGetTheSameResponseButOnlyKnownOnesReceiveMail() throws Exception {
        createUser("reset-user", "reset@example.com", true);

        forgot("reset@example.com").andExpect(status().isAccepted());
        forgot("nobody@example.com").andExpect(status().isAccepted());

        verify(mailSender, times(1)).sendResetLink(
                eq("reset@example.com"), eq("reset-user"), anyString(), anyInt());
        verify(mailSender, never()).sendResetLink(
                eq("nobody@example.com"), anyString(), anyString(), anyInt());
    }

    @Test
    void inactiveAccountsReceiveNoMail() throws Exception {
        createUser("inactive-user", "inactive@example.com", false);

        forgot("inactive@example.com").andExpect(status().isAccepted());

        verify(mailSender, never()).sendResetLink(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void validTokenChangesThePasswordAndCanOnlyBeUsedOnce() throws Exception {
        createUser("reset-user", "reset@example.com", true);
        String token = requestTokenFor("reset@example.com");

        reset(token, NEW_PASSWORD).andExpect(status().isOk());

        User updated = userRepository.findByUsername("reset-user").orElseThrow();
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updated.getPassword()));
        assertFalse(passwordEncoder.matches(OLD_PASSWORD, updated.getPassword()));

        reset(token, "AnotherPass3#").andExpect(status().isBadRequest());
        login("reset-user", NEW_PASSWORD).andExpect(status().isOk());
        login("reset-user", OLD_PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredAndUnknownTokensAreRejectedWithoutChangingThePassword() throws Exception {
        createUser("reset-user", "reset@example.com", true);
        String token = requestTokenFor("reset@example.com");

        PasswordResetToken stored = tokenRepository.findAll().get(0);
        stored.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(stored);

        reset(token, NEW_PASSWORD).andExpect(status().isBadRequest());
        reset("not-a-real-token", NEW_PASSWORD).andExpect(status().isBadRequest());

        User unchanged = userRepository.findByUsername("reset-user").orElseThrow();
        assertTrue(passwordEncoder.matches(OLD_PASSWORD, unchanged.getPassword()));
    }

    @Test
    void weakOrMissingInputIsRejectedByValidation() throws Exception {
        forgot("not-an-email").andExpect(status().isBadRequest());
        reset("some-token", "short").andExpect(status().isBadRequest());
    }

    @Test
    void repeatedRequestsWithinTheCooldownDoNotSendAnotherEmail() throws Exception {
        createUser("reset-user", "reset@example.com", true);

        forgot("reset@example.com").andExpect(status().isAccepted());
        forgot("reset@example.com").andExpect(status().isAccepted());

        verify(mailSender, times(1)).sendResetLink(anyString(), anyString(), anyString(), anyInt());
    }

    private String requestTokenFor(String email) throws Exception {
        forgot(email).andExpect(status().isAccepted());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailSender).sendResetLink(eq(email), anyString(), link.capture(), anyInt());
        String url = link.getValue();
        assertNotNull(url);
        assertTrue(url.contains("/reset-password?token="));
        return url.substring(url.indexOf("token=") + "token=".length());
    }

    private org.springframework.test.web.servlet.ResultActions forgot(String email) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions reset(String token, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions login(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }

    private void createUser(String username, String email, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(OLD_PASSWORD));
        user.setRole(Role.ATTENDEE);
        user.setActive(active);
        userRepository.save(user);
    }
}
