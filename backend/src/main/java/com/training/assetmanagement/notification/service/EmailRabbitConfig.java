package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailQueue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

// Khai exchange / queue giống hệt email-service
@Configuration
public class EmailRabbitConfig {

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EmailQueue.EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EmailQueue.QUEUE).build();
    }

    // Hết TTL của từng message -> dead-letter về email.queue
    @Bean
    public Queue emailRetryQueue() {
        return QueueBuilder.durable(EmailQueue.RETRY_QUEUE)
            .deadLetterExchange(EmailQueue.EXCHANGE)
            .deadLetterRoutingKey(EmailQueue.ROUTING_KEY)
            .build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(emailExchange()).with(EmailQueue.ROUTING_KEY);
    }

    @Bean
    public Binding emailRetryBinding() {
        return BindingBuilder.bind(emailRetryQueue()).to(emailExchange()).with(EmailQueue.RETRY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
