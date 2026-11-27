package com.training.assetmanagement.emailservice.messaging;

import com.training.assetmanagement.emailservice.config.EmailQueue;
import com.training.assetmanagement.emailservice.delivery.EmailDeliveryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final EmailDeliveryService deliveryService;

    @RabbitListener(queues = EmailQueue.QUEUE)
    public void onMessage(EmailMessage message,
                          @Header(name = EmailQueue.DEFERAL_HEADER, required = false) Integer deferrals) {
        try {
            deliveryService.deliver(message, deferrals == null ? 0 : deferrals);
        } catch (RuntimeException ex) {
            // KHÔNG BAO GIỜ ném ra ngoài: Mọi nhánh đều ack message
            log.error("Unexpected error while processing email log {} ({}) on RabbitMQ",
                message.emailLogId(), message.eventType(), ex);
        }
    }
}
