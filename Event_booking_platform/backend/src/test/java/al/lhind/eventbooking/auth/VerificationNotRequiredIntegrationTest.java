package al.lhind.eventbooking.auth;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import al.lhind.eventbooking.service.AccountMailSender;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(properties = "app.email-verification.required=false")
class VerificationNotRequiredIntegrationTest extends MySqlIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AccountMailSender mailSender;

    @Test
    void accountsCanSignInImmediatelyAndNoEmailIsSent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"quick\",\"email\":\"quick@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"quick\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk());

        verify(mailSender, never()).sendVerificationLink(anyString(), anyString(), anyString(), anyInt());
    }
}
