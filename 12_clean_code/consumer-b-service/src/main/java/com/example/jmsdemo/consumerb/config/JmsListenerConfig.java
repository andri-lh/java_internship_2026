package com.example.jmsdemo.consumerb.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
public class JmsListenerConfig {

    @Bean(name = "queueFactory")
    public DefaultJmsListenerContainerFactory queueFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // Queue mode: competing consumers share work from the same destination.
        factory.setPubSubDomain(false);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean(name = "topicFactory")
    public DefaultJmsListenerContainerFactory topicFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // Apply after configurer to avoid Boot defaults overriding topic mode.
        factory.setPubSubDomain(true);
        return factory;
    }
}
