package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailMessage;
import com.training.assetmanagement.notification.domain.EmailQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class EmailPublisherImpl implements EmailPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmailPublisherImpl.class);

    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishAfterCommit(EmailMessage message) {
        eventPublisher.publishEvent(message);
    }

    // Sau commit, trên luồng @Async: Giao dịch rollback thì không có email; broker sập không làm request treo
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendToBroker(EmailMessage message) {
        try {
            rabbitTemplate.convertAndSend(EmailQueue.EXCHANGE, EmailQueue.ROUTING_KEY, message);
        } catch (AmqpException ex) {
            // Phase 2: Đánh dấu email_logs FAILED + last_error trong giao dịch REQUIRES_NEW
            log.error("Failed to send email {} to {} on RabbitMQ", message.eventType(), message.recipientEmail(), ex);
        }
    }
}
