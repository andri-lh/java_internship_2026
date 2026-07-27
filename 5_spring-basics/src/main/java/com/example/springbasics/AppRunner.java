package com.example.springbasics;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {


    public AppRunner(NotificationManager notificationManager) {
        notificationManager.notifyUser("Welcome to Spring!");
    }

    @Override
    public void run(String... args) throws Exception {

    }

}
