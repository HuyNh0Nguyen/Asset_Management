package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailLog;
import com.training.assetmanagement.notification.domain.EmailMessage;
import com.training.assetmanagement.notification.domain.EmailNotification;
import com.training.assetmanagement.notification.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisherImpl.class);

    private final EmailLogRepository emailLogRepository;
    private final EmailPublisher emailPublisher;
    private final Clock clock;

    // MANDATORY: Dòng email_logs nằm trong giao dịch nghiệp vụ - rollback thì không có email
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishEmailOnly(EmailNotification notification) {
        EmailLog emailLog = emailLogRepository.save(EmailLog.pending(
            notification.recipientId(), notification.recipientEmail(), notification.eventType(),
            notification.subject(), LocalDateTime.now(clock)
        ));

        emailPublisher.publishAfterCommit(new EmailMessage(
            emailLog.getId(),
            notification.eventType(),
            notification.recipientEmail(),
            notification.recipientName(),
            notification.subject(),
            notification.eventType().templateName(),
            notification.link(),
            notification.templateVars()
        ));

        // Chỉ log id + loại - không bao giờ log nguyên EmailMessage (toString in cả templateVars)
        log.info("Email {} queued: event={}, recipientId={}",
            emailLog.getId(), notification.eventType(), notification.recipientId());
    }
}
