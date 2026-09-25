package com.example.jmsdemo.consumera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class ConsumerAServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerAServiceApplication.class, args);
    }
}
