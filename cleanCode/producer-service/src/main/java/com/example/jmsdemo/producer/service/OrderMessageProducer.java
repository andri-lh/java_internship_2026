package com.example.jmsdemo.producer.service;

import com.example.jmsdemo.producer.config.JmsDestinationConstants;
import com.example.jmsdemo.producer.exception.MessageSerializationException;
import com.example.jmsdemo.producer.model.OrderEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderMessageProducer.class);

    private final JmsTemplate queueJmsTemplate;
    private final JmsTemplate topicJmsTemplate;
    private final ObjectMapper objectMapper;

    public OrderMessageProducer(
            @Qualifier("jmsTemplate") JmsTemplate queueJmsTemplate,
            @Qualifier("topicJmsTemplate") JmsTemplate topicJmsTemplate,
            ObjectMapper objectMapper
    ) {
        this.queueJmsTemplate = queueJmsTemplate;
        this.topicJmsTemplate = topicJmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendToQueue(OrderEvent event) {
        String payload = toJson(event);
        queueJmsTemplate.convertAndSend(JmsDestinationConstants.ORDERS_QUEUE, payload);
        LOGGER.info("Published to queue [{}]: {}", JmsDestinationConstants.ORDERS_QUEUE, payload);
    }

    public void publishToTopic(OrderEvent event) {
        String payload = toJson(event);
        topicJmsTemplate.convertAndSend(JmsDestinationConstants.ORDERS_TOPIC, payload);
        LOGGER.info("Published to topic [{}]: {}", JmsDestinationConstants.ORDERS_TOPIC, payload);
    }

    private String toJson(OrderEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new MessageSerializationException("Failed to serialize OrderEvent to JSON", ex);
        }
    }
}
