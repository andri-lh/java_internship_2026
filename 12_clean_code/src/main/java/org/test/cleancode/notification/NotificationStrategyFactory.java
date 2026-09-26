package org.test.cleancode.notification;

import org.springframework.stereotype.Component;
import org.test.cleancode.exception.InvalidRegistrationException;

@Component
// Spring beans use singleton scope by default.
public class NotificationStrategyFactory {
    private final EmailNotificationStrategy emailNotificationStrategy;
    private final SmsNotificationStrategy smsNotificationStrategy;

    public NotificationStrategyFactory(
            EmailNotificationStrategy emailNotificationStrategy,
            SmsNotificationStrategy smsNotificationStrategy
    ) {
        this.emailNotificationStrategy = emailNotificationStrategy;
        this.smsNotificationStrategy = smsNotificationStrategy;
    }

    public NotificationStrategy getStrategy(NotificationType notificationType) {
        if (notificationType == null) {
            return emailNotificationStrategy;
        }
        if (notificationType == NotificationType.EMAIL) {
            return emailNotificationStrategy;
        }
        if (notificationType == NotificationType.SMS) {
            return smsNotificationStrategy;
        }
        throw new InvalidRegistrationException("Unsupported notification type: " + notificationType);
    }
}
