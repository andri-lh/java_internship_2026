package org.test.cleancode;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.test.cleancode.service.UserRegistrationService;

@SpringBootApplication
public class CleanCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanCodeApplication.class, args);
    }

    @Bean
    CommandLineRunner singletonDemo(ApplicationContext applicationContext) {
        return args -> {
            UserRegistrationService first = applicationContext.getBean(UserRegistrationService.class);
            UserRegistrationService second = applicationContext.getBean(UserRegistrationService.class);
            System.out.println("UserRegistrationService singleton: " + (first == second));
        };
    }
}
