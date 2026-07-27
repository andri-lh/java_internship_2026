package com.example.springbasics;

import org.springframework.stereotype.Service;

@Service
public class SmsNotifcationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("Sending SMS notification: " + message);
    }



}
