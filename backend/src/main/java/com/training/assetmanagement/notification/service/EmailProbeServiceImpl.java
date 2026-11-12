package com.training.assetmanagement.notification.service;

import com.training.assetmanagement.notification.domain.EmailEventType;
import com.training.assetmanagement.notification.domain.EmailMessage;
import com.training.assetmanagement.shared.config.AppProfile;
import com.training.assetmanagement.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Profile({AppProfile.LOCAL, AppProfile.DEV})
@RequiredArgsConstructor
public class EmailProbeServiceImpl implements EmailProbeService {

    private static final String PROBE_SUBJECT = "[Asset Management] Email Probe";
    private static final DateTimeFormatter SENT_AT_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final EmailPublisher emailPublisher;
    private final AppProperties properties;
    private final Clock clock;

    // Đi đúng đường của email nghiệp vụ: Trong giao dịch -> sau commit -> RabbitMQ
    @Override
    @Transactional
    public void sendProbe(String recipientEmail, String recipientName) {
        EmailEventType eventType = EmailEventType.EMAIL_PROBE;
        emailPublisher.publishAfterCommit(new EmailMessage(
            null,
            eventType,
            recipientEmail,
            recipientName,
            PROBE_SUBJECT,
            eventType.templateName(),
            properties.publicBaseUrl(),
            Map.of("sentAt", LocalDateTime.now(clock).format(SENT_AT_FORMAT))
        ));
    }
}
