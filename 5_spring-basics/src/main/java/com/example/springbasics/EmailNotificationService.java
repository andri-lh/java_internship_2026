package com.example.springbasics;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
public class EmailNotificationService implements NotificationService
{

    @Value("${notification.prefix}")
    public void send(String message) {
        System.out.println(message);
    }
}
