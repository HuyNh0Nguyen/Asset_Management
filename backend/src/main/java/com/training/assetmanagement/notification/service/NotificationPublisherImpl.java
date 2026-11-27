package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.*;
import com.training.assetmanagement.notification.repository.EmailLogRepository;
import com.training.assetmanagement.notification.repository.NotificationRepository;
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

    private final NotificationRepository notificationRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailPublisher emailPublisher;
    private final Clock clock;

    // MANDATORY: Dòng notifications nằm trong giao dịch nghiệp vụ - rollback thì không có thông báo
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishInAppOnly(InAppNotification notification) {
        NotificationChannelRules.requireInAppOnly(notification.eventType());
        saveInApp(notification, LocalDateTime.now(clock));
    }

    // MANDATORY: Dòng email_logs nằm trong giao dịch nghiệp vụ - rollback thì không có email
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishEmailOnly(EmailNotification notification) {
        NotificationChannelRules.requireEmailOnly(notification.eventType());
        queueEmail(notification, LocalDateTime.now(clock));
    }

    // Broker sập sau commit chỉ làm email FAILED - dòng in-app vẫn còn
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(InAppNotification inApp, EmailNotification email) {
        NotificationChannelRules.requireBothChannels(inApp, email);
        LocalDateTime now = LocalDateTime.now(clock);
        saveInApp(inApp, now);
        queueEmail(email, now);
    }

    private void saveInApp(InAppNotification notification, LocalDateTime now) {
        Notification saved = notificationRepository.save(Notification.unread(notification, now));
        log.info("Notification {} created: event={}, recipientId={}",
            saved.getId(), notification.eventType(), notification.recipientId());
    }

    // Chèn PENDING rồi hẹn publish sau commit
    private void queueEmail(EmailNotification notification, LocalDateTime now) {
        EmailLog emailLog = emailLogRepository.save(EmailLog.pending(
            notification.recipientId(), notification.recipientEmail(), notification.eventType(),
            notification.subject(), now
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

        // Chỉ log id + loại - không bao giờ log nguyên EmailMessage (templateVars có mật khẩu tạm)
        log.info("Email {} queued: event={}, recipientId={}",
            emailLog.getId(), notification.eventType(), notification.recipientId());
    }
}
