package com.example.jmsdemo.producer.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(
        String orderId,
        String customerName,
        BigDecimal amount,
        Instant createdAt
) {
}
