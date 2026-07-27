package com.example.springbasics;

import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {

    private NotificationService notificationService;

    @Override
    public void send(String message) {
        notificationService.send("Sending SMS notification: " + message);
    }



}
