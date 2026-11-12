package com.training.assetmanagement.emailservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

// Khai giống hệt backend - bên nào khởi động trước cũng tạo đúng một cấu trúc
@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EmailQueue.EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EmailQueue.QUEUE).build();
    }

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
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        // Header __TypeId__ trỏ tới lớp chỉ có trong backend -> bỏ qua, suy kiểu từ tham số listener
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
