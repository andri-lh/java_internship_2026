package com.example.springbasics;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class NotificationManager {

    private final NotificationService notificationService;


    public NotificationManager(@Qualifier("smsNotificationService") NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyUser(String message) {
        notificationService.send(message);
    }

}
