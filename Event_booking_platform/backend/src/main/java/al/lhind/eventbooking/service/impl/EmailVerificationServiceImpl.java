package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.dto.request.ResendVerificationRequest;
import al.lhind.eventbooking.dto.request.VerifyEmailRequest;
import al.lhind.eventbooking.entity.EmailVerificationToken;
import al.lhind.eventbooking.entity.User;
import al.lhind.eventbooking.exception.InvalidRequestException;
import al.lhind.eventbooking.repository.EmailVerificationTokenRepository;
import al.lhind.eventbooking.repository.UserRepository;
import al.lhind.eventbooking.service.AccountMailSender;
import al.lhind.eventbooking.service.EmailVerificationService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private static final int TOKEN_BYTES = 32;
    private static final int RESEND_COOLDOWN_MINUTES = 1;
    private static final String INVALID_LINK = "This verification link is invalid or has expired";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AccountMailSender mailSender;
    private final String frontendBaseUrl;
    private final int validHours;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationServiceImpl(
            UserRepository userRepository,
            EmailVerificationTokenRepository tokenRepository,
            AccountMailSender mailSender,
            @Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl,
            @Value("${app.email-verification.expiration-hours:24}") int validHours) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.validHours = validHours;
    }

    @Override
    @Transactional
    public void sendVerification(User user) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredOrUsed(now);
        tokenRepository.deleteAllByUserId(user.getId());

        byte[] random = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(random);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusHours(validHours));
        tokenRepository.save(token);

        String link = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/verify-email")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        mailSender.sendVerificationLink(user.getEmail(), user.getUsername(), link, validHours);
        log.info("Email verification link issued: userId={}", user.getId());
    }

    @Override
    @Transactional
    public void resend(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email().trim())
                .filter(User::isActive)
                .filter(found -> !found.isEmailVerified())
                .orElse(null);

        if (user == null) {
            log.info("Verification resend requested for an unknown, inactive, or already verified account");
            return;
        }

        if (tokenRepository.existsByUserIdAndUsedAtIsNullAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusMinutes(RESEND_COOLDOWN_MINUTES))) {
            log.info("Verification resend throttled: userId={}", user.getId());
            return;
        }

        sendVerification(user);
    }

    @Override
    @Transactional
    public void verify(VerifyEmailRequest request) {
        EmailVerificationToken token = tokenRepository
                .findByTokenHashWithUser(hash(request.token().trim()))
                .orElseThrow(() -> new InvalidRequestException(INVALID_LINK));

        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw new InvalidRequestException(INVALID_LINK);
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(now);
        tokenRepository.save(token);
        log.info("Email verified: userId={}", user.getId());
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
