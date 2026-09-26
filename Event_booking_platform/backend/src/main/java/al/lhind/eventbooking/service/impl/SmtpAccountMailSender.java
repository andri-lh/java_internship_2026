package al.lhind.eventbooking.service.impl;

import al.lhind.eventbooking.service.AccountMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpAccountMailSender implements AccountMailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpAccountMailSender.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public SmtpAccountMailSender(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:no-reply@eventbooking.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendResetLink(String toEmail, String username, String resetLink, int validMinutes) {
        send(toEmail, "Reset your EventBooking password",
                "Hello " + username + ",\n\n"
                        + "We received a request to reset your password. Use the link below to choose a new one. "
                        + "It is valid for " + validMinutes + " minutes and can be used once.\n\n"
                        + resetLink + "\n\n"
                        + "If you did not request this, you can safely ignore this email.");
    }

    @Override
    public void sendVerificationLink(String toEmail, String username, String verificationLink, int validHours) {
        send(toEmail, "Confirm your EventBooking email address",
                "Welcome to EventBooking, " + username + "!\n\n"
                        + "Confirm your email address to activate sign-in. "
                        + "The link is valid for " + validHours + " hours.\n\n"
                        + verificationLink + "\n\n"
                        + "If you did not create this account, you can ignore this email.");
    }

    private void send(String toEmail, String subject, String text) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Email not sent because no mail server is configured (spring.mail.host)");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            sender.send(message);
        } catch (MailException exception) {
            log.error("Email could not be sent", exception);
        }
    }
}
