package al.lhind.eventbooking.service;

public interface PasswordResetMailSender {

    void sendResetLink(String toEmail, String username, String resetLink, int validMinutes);
}
