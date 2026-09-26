package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.exception.AuthenticationFailureException;
import al.lhind.eventbooking.exception.ForbiddenOperationException;
import al.lhind.eventbooking.exception.InvalidRequestException;
import al.lhind.eventbooking.service.EmailVerificationService;
import al.lhind.eventbooking.exception.BusinessConflictException;
import al.lhind.eventbooking.dto.request.ChangePasswordRequest;
import al.lhind.eventbooking.dto.request.LoginRequest;
import al.lhind.eventbooking.dto.request.RegisterRequest;
import al.lhind.eventbooking.dto.response.AuthResponse;
import al.lhind.eventbooking.dto.response.UserResponse;
import al.lhind.eventbooking.entity.Role;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.security.JwtTokenService;
import al.lhind.eventbooking.service.AuthService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailVerificationService emailVerificationService;
    private final boolean emailVerificationRequired;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService,
                           EmailVerificationService emailVerificationService,
                           @Value("${app.email-verification.required:true}") boolean emailVerificationRequired) {
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationRequired = emailVerificationRequired;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessConflictException("Username already exists");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ATTENDEE);
        user.setActive(true);
        user.setEmailVerified(!emailVerificationRequired);

        User savedUser = userRepository.save(user);
        log.info("User registered: userId={}, role={}", savedUser.getId(), savedUser.getRole());
        if (emailVerificationRequired) {
            emailVerificationService.sendVerification(savedUser);
        }

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isActive()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailureException("Invalid username or password");
        }

        if (!user.isEmailVerified()) {
            throw new ForbiddenOperationException("Please verify your email address before signing in");
        }

        String token = jwtTokenService.generateToken(user);
        log.info("User logged in: userId={}, role={}", user.getId(), user.getRole());

        return new AuthResponse(token, "Bearer", user.getRole());
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailureException("Active user not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidRequestException("The new password must be different from the current one");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed: userId={}", user.getId());
    }
}
