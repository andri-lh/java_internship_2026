package org.test.cleancode.notification;

import org.springframework.stereotype.Component;
import org.test.cleancode.domain.User;

@Component
// Spring beans use singleton scope by default.
public class EmailNotificationStrategy implements NotificationStrategy {
    @Override
    public void sendWelcomeMessage(User user) {
        System.out.println("Email welcome message sent to " + user.getEmail());
    }
}
