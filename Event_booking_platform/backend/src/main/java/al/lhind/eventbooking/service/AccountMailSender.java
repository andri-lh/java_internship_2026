package al.lhind.eventbooking.service;

public interface AccountMailSender {

    void sendResetLink(String toEmail, String username, String resetLink, int validMinutes);

    void sendVerificationLink(String toEmail, String username, String verificationLink, int validHours);
}
