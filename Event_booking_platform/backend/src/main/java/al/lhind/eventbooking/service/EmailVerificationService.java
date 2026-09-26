package al.lhind.eventbooking.service;

import al.lhind.eventbooking.dto.request.ResendVerificationRequest;
import al.lhind.eventbooking.dto.request.VerifyEmailRequest;
import al.lhind.eventbooking.entity.User;

public interface EmailVerificationService {

    void sendVerification(User user);

    void resend(ResendVerificationRequest request);

    void verify(VerifyEmailRequest request);
}
