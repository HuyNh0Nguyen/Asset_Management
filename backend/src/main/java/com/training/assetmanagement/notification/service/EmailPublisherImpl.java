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
    private final EmailLogService emailLogService;

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
            // Không log nguyên message: templateVars có thể chứa mật khẩu tạm
            log.error("Failed to send email {} ({}) to {} on RabbitMQ",
                message.emailLogId(), message.eventType(), message.recipientEmail(), ex);
            markFailed(message, ex);
        }
    }

    // Dòng email_logs thành FAILED để tra được; in-app (nếu có) đã nằm trong CSDL
    private void markFailed(EmailMessage message, AmqpException ex) {
        // Email thử của phase 0 không có dùng email_logs
        if (message.emailLogId() == null) {
            return;
        }
        try {
            emailLogService.markFailed(message.emailLogId(), ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } catch (RuntimeException markException) {
            log.error("Failed to mark email log {} as FAILED", message.emailLogId(), markException);
        }
    }
}
