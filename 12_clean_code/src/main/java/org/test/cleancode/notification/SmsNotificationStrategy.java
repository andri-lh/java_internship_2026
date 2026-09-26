package org.test.cleancode.notification;

import org.springframework.stereotype.Component;
import org.test.cleancode.domain.User;

@Component
// Spring beans use singleton scope by default.
public class SmsNotificationStrategy implements NotificationStrategy {
    @Override
    public void sendWelcomeMessage(User user) {
        System.out.println("SMS welcome message sent to " + user.getName());
    }
}
