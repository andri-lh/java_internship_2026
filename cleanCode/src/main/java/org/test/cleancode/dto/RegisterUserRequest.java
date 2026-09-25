package org.test.cleancode.dto;

import org.test.cleancode.notification.NotificationType;

public class RegisterUserRequest {
    private String name;
    private String email;
    private NotificationType notificationType;

    public RegisterUserRequest() {
    }

    public RegisterUserRequest(String name, String email, NotificationType notificationType) {
        this.name = name;
        this.email = email;
        this.notificationType = notificationType;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
