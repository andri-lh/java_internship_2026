package com.example.jmsdemo.producer.controller;

import com.example.jmsdemo.producer.model.OrderEvent;
import com.example.jmsdemo.producer.service.OrderMessageProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class OrderMessageController {

    private final OrderMessageProducer orderMessageProducer;

    public OrderMessageController(OrderMessageProducer orderMessageProducer) {
        this.orderMessageProducer = orderMessageProducer;
    }

    @PostMapping("/queue")
    public ResponseEntity<OrderEvent> sendToQueue(
            @RequestParam("customer") String customer,
            @RequestParam("amount") BigDecimal amount
    ) {
        OrderEvent event = createEvent(customer, amount);
        orderMessageProducer.sendToQueue(event);
        return ResponseEntity.accepted().body(event);
    }

    @PostMapping("/topic")
    public ResponseEntity<OrderEvent> publishToTopic(
            @RequestParam("customer") String customer,
            @RequestParam("amount") BigDecimal amount
    ) {
        OrderEvent event = createEvent(customer, amount);
        orderMessageProducer.publishToTopic(event);
        return ResponseEntity.accepted().body(event);
    }

    @PostMapping("/both")
    public ResponseEntity<OrderEvent> sendToBoth(
            @RequestParam("customer") String customer,
            @RequestParam("amount") BigDecimal amount
    ) {
        // Same event instance is sent to queue and topic to compare semantics.
        OrderEvent event = createEvent(customer, amount);
        orderMessageProducer.sendToQueue(event);
        orderMessageProducer.publishToTopic(event);
        return ResponseEntity.accepted().body(event);
    }

    private OrderEvent createEvent(String customer, BigDecimal amount) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                customer,
                amount,
                Instant.now()
        );
    }
}
