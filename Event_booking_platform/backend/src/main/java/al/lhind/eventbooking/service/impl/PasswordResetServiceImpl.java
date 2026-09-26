package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.ForgotPasswordRequest;
import al.lhind.eventbooking.dto.request.ResetPasswordRequest;
import al.lhind.eventbooking.entity.PasswordResetToken;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.exception.InvalidRequestException;
import al.lhind.eventbooking.repository.PasswordResetTokenRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.PasswordResetMailSender;
import al.lhind.eventbooking.service.PasswordResetService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int TOKEN_BYTES = 32;
    private static final int RESEND_COOLDOWN_MINUTES = 1;
    private static final String INVALID_LINK = "This password reset link is invalid or has expired";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailSender mailSender;
    private final String frontendBaseUrl;
    private final int validMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetMailSender mailSender,
            @Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl,
            @Value("${app.password-reset.expiration-minutes:30}") int validMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.validMinutes = validMinutes;
    }

    @Override
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email().trim())
                .filter(User::isActive)
                .orElse(null);

        if (user == null) {
            log.info("Password reset requested for an unknown or inactive account");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (tokenRepository.existsByUserIdAndUsedAtIsNullAndCreatedAtAfter(
                user.getId(), now.minusMinutes(RESEND_COOLDOWN_MINUTES))) {
            log.info("Password reset request throttled: userId={}", user.getId());
            return;
        }

        tokenRepository.deleteExpiredOrUsed(now);
        tokenRepository.deleteAllByUserId(user.getId());

        byte[] random = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(random);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusMinutes(validMinutes));
        tokenRepository.save(token);

        String link = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        mailSender.sendResetLink(user.getEmail(), user.getUsername(), link, validMinutes);
        log.info("Password reset link issued: userId={}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByTokenHashWithUser(hash(request.token().trim()))
                .orElseThrow(() -> new InvalidRequestException(INVALID_LINK));

        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now) || !token.getUser().isActive()) {
            throw new InvalidRequestException(INVALID_LINK);
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsedAt(now);
        tokenRepository.save(token);
        log.info("Password reset completed: userId={}", user.getId());
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
