package al.lhind.eventbooking.controller;

import al.lhind.eventbooking.dto.request.ForgotPasswordRequest;
import al.lhind.eventbooking.dto.request.LoginRequest;
import al.lhind.eventbooking.dto.request.RegisterRequest;
import al.lhind.eventbooking.dto.request.ResendVerificationRequest;
import al.lhind.eventbooking.dto.request.VerifyEmailRequest;
import al.lhind.eventbooking.dto.request.ResetPasswordRequest;
import al.lhind.eventbooking.dto.response.AuthResponse;
import al.lhind.eventbooking.dto.response.MessageResponse;
import al.lhind.eventbooking.dto.response.UserResponse;
import al.lhind.eventbooking.service.AuthService;
import al.lhind.eventbooking.service.EmailVerificationService;
import al.lhind.eventbooking.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Register an attendee account and obtain a JWT.")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService,
                          EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Register a new attendee")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Log in and receive a JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Request a password reset email",
            description = "Always answers 202, whether or not the email belongs to an account.")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse(
                "If an account exists for that email, a reset link has been sent."));
    }

    @Operation(summary = "Set a new password using a reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Your password has been updated."));
    }

    @Operation(summary = "Confirm an email address using a verification token")
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request);
        return ResponseEntity.ok(new MessageResponse("Your email address has been verified."));
    }

    @Operation(summary = "Resend the verification email",
            description = "Always answers 202, whether or not the email needs verification.")
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resend(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse(
                "If that account still needs verification, a new link has been sent."));
    }

}
