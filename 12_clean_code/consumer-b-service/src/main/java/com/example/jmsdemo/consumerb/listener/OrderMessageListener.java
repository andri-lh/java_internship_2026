package com.example.jmsdemo.consumerb.listener;

import com.example.jmsdemo.consumerb.config.JmsDestinationConstants;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageListener {

    @JmsListener(
            destination = JmsDestinationConstants.ORDERS_QUEUE,
            containerFactory = "queueFactory"
    )
    public void onQueueMessage(String message) {
        print("Consumer B", "QUEUE", message);
    }

    @JmsListener(
            destination = JmsDestinationConstants.ORDERS_TOPIC,
            containerFactory = "topicFactory"
    )
    public void onTopicMessage(String message) {
        print("Consumer B", "TOPIC", message);
    }

    private void print(String consumer, String source, String message) {
        System.out.println("==================================================");
        System.out.println(" " + consumer + " RECEIVED MESSAGE ");
        System.out.println(" Source : " + source);
        System.out.println(" Body   : " + message);
        System.out.println("==================================================");
    }
}
