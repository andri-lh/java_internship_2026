package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.service.PasswordResetMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetMailSender implements PasswordResetMailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpPasswordResetMailSender.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public SmtpPasswordResetMailSender(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:no-reply@eventbooking.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendResetLink(String toEmail, String username, String resetLink, int validMinutes) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Password reset email not sent because no mail server is configured (spring.mail.host)");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Reset your EventBooking password");
        message.setText("Hello " + username + ",\n\n"
                + "We received a request to reset your password. Use the link below to choose a new one. "
                + "It is valid for " + validMinutes + " minutes and can be used once.\n\n"
                + resetLink + "\n\n"
                + "If you did not request this, you can safely ignore this email.");

        try {
            sender.send(message);
        } catch (MailException exception) {
            log.error("Password reset email could not be sent", exception);
        }
    }
}
