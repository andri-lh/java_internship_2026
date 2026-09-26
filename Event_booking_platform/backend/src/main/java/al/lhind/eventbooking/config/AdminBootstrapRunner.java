package al.lhind.eventbooking.config;

import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator in an empty database from configuration. Does nothing unless all three
 * values are set, and never touches an installation that already has an admin.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username:}") String username,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username.trim();
        this.email = email.trim();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureAdmin();
    }

    public void ensureAdmin() {
        if (username.isEmpty() && email.isEmpty() && password.isEmpty()) {
            return;
        }
        if (username.length() < 3 || email.isEmpty() || password.length() < 8) {
            log.warn("Bootstrap admin skipped: username (3+ characters), email, and password (8+ characters) are all required");
            return;
        }
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Bootstrap admin skipped: an administrator already exists");
            return;
        }
        if (userRepository.existsByUsernameIgnoreCase(username) || userRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Bootstrap admin skipped: the configured username or email is already taken");
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(true);
        User saved = userRepository.save(admin);
        log.info("Bootstrap admin created: userId={}", saved.getId());
    }
}
