package com.example.jmsdemo.consumerb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class ConsumerBServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerBServiceApplication.class, args);
    }
}
