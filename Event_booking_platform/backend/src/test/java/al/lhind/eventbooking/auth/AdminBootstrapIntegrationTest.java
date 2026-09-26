package al.lhind.eventbooking.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import al.lhind.eventbooking.config.AdminBootstrapRunner;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.support.MySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminBootstrapIntegrationTest extends MySqlIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void createsAVerifiedActiveAdminInAnEmptyDatabase() {
        runner("first-admin", "first@example.com", "Password123!").ensureAdmin();

        User admin = userRepository.findByUsername("first-admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertTrue(admin.isEmailVerified());
        assertTrue(passwordEncoder.matches("Password123!", admin.getPassword()));
    }

    @Test
    void doesNothingWhenNotConfigured() {
        runner("", "", "").ensureAdmin();

        assertEquals(0, userRepository.count());
    }

    @Test
    void doesNothingWhenConfigurationIsIncomplete() {
        runner("first-admin", "first@example.com", "short").ensureAdmin();
        runner("first-admin", "", "Password123!").ensureAdmin();

        assertEquals(0, userRepository.count());
    }

    @Test
    void neverCreatesASecondAdmin() {
        runner("first-admin", "first@example.com", "Password123!").ensureAdmin();
        runner("second-admin", "second@example.com", "Password123!").ensureAdmin();

        assertTrue(userRepository.findByUsername("first-admin").isPresent());
        assertFalse(userRepository.findByUsername("second-admin").isPresent());
    }

    private AdminBootstrapRunner runner(String username, String email, String password) {
        return new AdminBootstrapRunner(userRepository, passwordEncoder, username, email, password);
    }
}
