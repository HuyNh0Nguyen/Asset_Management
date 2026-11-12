package com.training.assetmanagement.emailservice.delivery;

import com.training.assetmanagement.emailservice.config.EmailProperties;
import com.training.assetmanagement.emailservice.config.EmailQueue;
import com.training.assetmanagement.emailservice.messaging.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.exceptions.TemplateEngineException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailDeliveryServiceImpl implements EmailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailRenderer renderer;
    private final RabbitTemplate rabbitTemplate;
    private final EmailProperties properties;

    @Override
    public void deliver(EmailMessage message, int attempt) {
        // TODO (Phase 2): Đọc email_logs theo emailLogId - không còn PENDING thì bỏ qua (chống gửi trùng)
        String html;
        try {
            html = renderer.render(message);
        } catch (TemplateEngineException ex) {
            // Lỗi mẫu: Thử lại cũng hỏng y như vậy -> dừng ngay (Phase 2: FAILED)
            log.error("Failed to render email {} for {}: {}", message.eventType(), message.recipientEmail(), ex.getMessage());
            return;
        }
        try {
            send(message, html);
            log.info("Email delivery completed successfully to {} for event {}", message.recipientEmail(), message.eventType());
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            handleSendFailure(message, attempt, ex);
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

    // Còn lượt -> đưa vào email.retry.queue với TTL; hết TTL RabbitMQ trả về email.queue
    private void handleSendFailure(EmailMessage message, int attempt, Exception ex) {
        if (attempt >= properties.maxRetries()) {
            log.error("Failed to send email {} for {}: {}", message.eventType(), message.recipientEmail(), ex.getMessage());
            return;
        }
        int nextAttempt = attempt + 1;
        log.warn("Failed to send email {} for {}: {}", message.eventType(), message.recipientEmail(), ex.getMessage());
        rabbitTemplate.convertAndSend(EmailQueue.EXCHANGE, EmailQueue.RETRY_ROUTING_KEY, message, amqpMessage -> {
            MessageProperties messageProperties = amqpMessage.getMessageProperties();
            messageProperties.setExpiration(String.valueOf(properties.retryDelay().toMillis()));
            messageProperties.setHeader(EmailQueue.ATTEMPT_HEADER, nextAttempt);
            return amqpMessage;
        });
    }
}
