package al.lhind.eventbooking.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.service.AccountMailSender;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class AccountFeaturesApiIntegrationTest extends MySqlIntegrationTest {

    private static final String PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenService jwtTokenService;

    @MockitoBean private AccountMailSender mailSender;

    @Test
    void newAccountsMustVerifyTheirEmailBeforeSigningIn() throws Exception {
        post("/api/v1/auth/register", "{\"username\":\"newbie\",\"email\":\"newbie@example.com\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isCreated());

        post("/api/v1/auth/login", "{\"username\":\"newbie\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isForbidden());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailSender).sendVerificationLink(eq("newbie@example.com"), eq("newbie"), link.capture(), anyInt());
        String token = link.getValue().substring(link.getValue().indexOf("token=") + 6);

        post("/api/v1/auth/verify-email", "{\"token\":\"" + token + "\"}").andExpect(status().isOk());
        assertTrue(userRepository.findByUsername("newbie").orElseThrow().isEmailVerified());

        post("/api/v1/auth/login", "{\"username\":\"newbie\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isOk());
        post("/api/v1/auth/verify-email", "{\"token\":\"" + token + "\"}").andExpect(status().isBadRequest());
    }

    @Test
    void unknownVerificationTokensAreRejected() throws Exception {
        post("/api/v1/auth/verify-email", "{\"token\":\"nope\"}").andExpect(status().isBadRequest());
    }

    @Test
    void resendGivesTheSameAnswerForEveryEmailAndOnlyMailsUnverifiedAccounts() throws Exception {
        User unverified = createUser("pending", "pending@example.com", false);
        createUser("verified", "verified@example.com", true);

        post("/api/v1/auth/resend-verification", "{\"email\":\"pending@example.com\"}").andExpect(status().isAccepted());
        post("/api/v1/auth/resend-verification", "{\"email\":\"verified@example.com\"}").andExpect(status().isAccepted());
        post("/api/v1/auth/resend-verification", "{\"email\":\"ghost@example.com\"}").andExpect(status().isAccepted());

        verify(mailSender).sendVerificationLink(eq(unverified.getEmail()), anyString(), anyString(), anyInt());
        verify(mailSender, never()).sendVerificationLink(eq("verified@example.com"), anyString(), anyString(), anyInt());
        verify(mailSender, never()).sendVerificationLink(eq("ghost@example.com"), anyString(), anyString(), anyInt());
    }

    @Test
    void signedInUserCanChangePasswordWithTheCurrentOne() throws Exception {
        User user = createUser("changer", "changer@example.com", true);
        String bearer = "Bearer " + jwtTokenService.generateToken(user);

        change(bearer, "wrong-current", "NewPassword2@").andExpect(status().isBadRequest());
        change(bearer, PASSWORD, PASSWORD).andExpect(status().isBadRequest());
        change(bearer, PASSWORD, "short").andExpect(status().isBadRequest());
        change(bearer, PASSWORD, "NewPassword2@").andExpect(status().isOk());

        User updated = userRepository.findByUsername("changer").orElseThrow();
        assertTrue(passwordEncoder.matches("NewPassword2@", updated.getPassword()));
        assertFalse(passwordEncoder.matches(PASSWORD, updated.getPassword()));
    }

    @Test
    void weakPasswordsAreRejectedByRegistrationAndChange() throws Exception {
        for (String weak : new String[] {"password123!", "PASSWORD123!", "Password!!!!", "Password1234", "Pass1!"}) {
            post("/api/v1/auth/register", "{\"username\":\"weak\",\"email\":\"weak@example.com\",\"password\":\"" + weak + "\"}")
                    .andExpect(status().isBadRequest());
        }

        User user = createUser("strengthcheck", "strength@example.com", true);
        change("Bearer " + jwtTokenService.generateToken(user), PASSWORD, "alllowercase1!")
                .andExpect(status().isBadRequest());
    }

    @Test
    void changingPasswordRequiresAuthentication() throws Exception {
        post("/api/v1/account/password", "{\"currentPassword\":\"a\",\"newPassword\":\"NewPassword2@\"}")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongCurrentPasswordDoesNotSignTheUserOut() throws Exception {
        User user = createUser("keeper", "keeper@example.com", true);
        change("Bearer " + jwtTokenService.generateToken(user), "bad", "NewPassword2@")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    private ResultActions change(String bearer, String current, String next) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/account/password")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next + "\"}"));
    }

    private ResultActions post(String url, String json) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private User createUser(String username, String email, boolean verified) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.ATTENDEE);
        user.setActive(true);
        user.setEmailVerified(verified);
        return userRepository.save(user);
    }
}
