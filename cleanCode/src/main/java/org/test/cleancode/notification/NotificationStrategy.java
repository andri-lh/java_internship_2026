package org.test.cleancode.notification;

import org.test.cleancode.domain.User;

public interface NotificationStrategy {
    void sendWelcomeMessage(User user);
}
