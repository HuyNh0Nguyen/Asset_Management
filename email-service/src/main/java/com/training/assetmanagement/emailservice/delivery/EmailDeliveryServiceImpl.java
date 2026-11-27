package com.training.assetmanagement.emailservice.delivery;

import com.training.assetmanagement.emailservice.config.EmailProperties;
import com.training.assetmanagement.emailservice.config.EmailQueue;
import com.training.assetmanagement.emailservice.log.EmailLog;
import com.training.assetmanagement.emailservice.log.EmailLogService;
import com.training.assetmanagement.emailservice.log.EmailLogStatus;
import com.training.assetmanagement.emailservice.messaging.EmailEventType;
import com.training.assetmanagement.emailservice.messaging.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.hibernate.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.exceptions.TemplateEngineException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailDeliveryServiceImpl implements EmailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailRenderer renderer;
    private final RabbitTemplate rabbitTemplate;
    private final EmailProperties properties;
    private final EmailLogService emailLogService;

    // Mọi nhánh kết thúc bình thường -> listener ack message
    @Override
    public void deliver(EmailMessage message, int deferrals) {
        if (message.emailLogId() == null) {
            deliverUntracked(message);
            return;
        }
        try {
            deliverTracked(message);
        } catch (DataAccessException | TransactionException ex) {
            // CSDL không tới được (kể cả lúc mở giao dịch) -> hoãn, không tăng attempt_count
            defer(message, deferrals, ex);
        }
    }

    // Bước 0: Email thử tới địa chỉ bất kỳ - gửi đúng một lần, không thử lại, không chạm CSDL
    private void deliverUntracked(EmailMessage message) {
        if (message.eventType() != EmailEventType.EMAIL_PROBE) {
            log.error("Dropping {} email without emailLogId", message.eventType());
            return;
        }
        try {
            send(message, renderer.render(message));
            log.info("Email probe delivered to {}", message.recipientEmail());
        } catch (TemplateEngineException | MailException | MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to deliver email probe to {}: {}", message.recipientEmail(), ex.getMessage());
        }
    }

    private void deliverTracked(EmailMessage message) {
        UUID emailLogId = message.emailLogId();

        // Bước 1: Không còn PENDING -> bỏ (chống gửi trùng khi message được giao lại)
        Optional<EmailLog> found = emailLogService.find(emailLogId);
        if (found.isEmpty()) {
            log.error("Email log {} not found, dropping {} message", emailLogId, message.eventType());
            return;
        }
        EmailLog emailLog = found.get();
        if (emailLog.getStatus() != EmailLogStatus.PENDING) {
            log.info("Email log {} already {}, skipping", emailLogId, emailLog.getStatus());
            return;
        }

        // Bước 2: Lỗi mẫu - lần sau cũng hỏng y như vậy -> FAILED ngay, không thử lại
        String html;
        try {
            html = renderer.render(message);
        } catch (TemplateEngineException ex) {
            log.error("Failed to render email log {} ({}): {}", emailLogId, message.eventType(), ex.getMessage());
            emailLogService.markFailed(emailLogId, EmailFailureKind.TEMPLATE.describe(ex.getMessage()));
            return;
        }

        // Bước 3: Gửi SMTP
        try {
            send(message, html);
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            handleSendFailure(message, emailLog.getAttemptCount(), ex);
            return;
        }
        if (emailLogService.markSent(emailLogId)) {
            log.info("Email log {} sent ({}) after {} retries", emailLogId, message.eventType(), emailLog.getAttemptCount());
        } else {
            log.warn("Email log {} was finalized elsewhere after sending", emailLogId);
        }
    }

    private void send(EmailMessage message, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
        helper.setFrom(properties.fromAddress(), properties.fromName());
        helper.setTo(message.recipientEmail());
        helper.setSubject(message.subject());
        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }

    // Bước 4-5: Còn lượt -> CAS tăng attempt_count rồi hẹn lại; hết lượt -> FAILED, giữ câu trả lời của Gmail
    private void handleSendFailure(EmailMessage message, int attemptCount, Exception ex) {
        UUID emailLogId = message.emailLogId();
        String lastError = EmailFailureKind.SMTP.describe(ex.getMessage());
        if (attemptCount >= properties.maxRetries()) {
            log.error("Email log {} failed after {} retries: {}", emailLogId, attemptCount, ex.getMessage());
            emailLogService.markFailed(emailLogId, lastError);
            return;
        }
        if (!emailLogService.scheduleRetry(emailLogId, attemptCount, lastError)) {
            log.warn("Email log {} changed concurrently, retry not scheduled", emailLogId);
            return;
        }
        log.warn("Email log {} failed, retry {}/{} in {}: {}",
            emailLogId, attemptCount + 1, properties.maxRetries(), properties.retryDelay(), ex.getMessage());
        try {
            pushToRetryQueue(message, null);
        } catch (AmqpException amqpEx) {
            // Message đã mất -> không để dòng kẹt PENDING
            log.error("Failed to schedule retry for email log {}", emailLogId, amqpEx);
            emailLogService.markFailed(emailLogId, EmailFailureKind.BROKER.describe(amqpEx.getMessage()));
        }
    }

    // Bước 6: Hoãn có trần, quá trần thì bỏ - dòng vẫn PENDING, tra được bằng idx_email_logs_status
    private void defer(EmailMessage message, int deferrals, RuntimeException ex) {
        if (deferrals >= properties.maxDeferrals()) {
            log.error("Database unavailable, dropping email log {} after {} deferrals",
                message.emailLogId(), deferrals, ex);
            return;
        }
        log.warn("Database unavailable, deferring email log {} ({}/{}): {}",
            message.emailLogId(), deferrals + 1, properties.maxDeferrals(), ex.getMessage());
        pushToRetryQueue(message, deferrals + 1);
    }

    // Hết TTL, RabbitMQ dead-letter về email.queue
    // deferral = null: CSDL vừa ghi được -> không gắn header, đếm hoãn về 0
    private void pushToRetryQueue(EmailMessage message, Integer deferral) {
        rabbitTemplate.convertAndSend(EmailQueue.EXCHANGE, EmailQueue.RETRY_ROUTING_KEY, message, amqpMessage -> {
            MessageProperties messageProperties = amqpMessage.getMessageProperties();
            messageProperties.setExpiration(String.valueOf(properties.retryDelay().toMillis()));
            if (deferral != null) {
                messageProperties.setHeader(EmailQueue.DEFERAL_HEADER, deferral);
            }
            return amqpMessage;
        });
    }
}
