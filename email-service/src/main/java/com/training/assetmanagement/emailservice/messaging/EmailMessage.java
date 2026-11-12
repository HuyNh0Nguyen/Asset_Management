package com.training.assetmanagement.emailservice.messaging;

import java.util.Map;
import java.util.UUID;

// Khớp contracts/email-message.example.json
public record EmailMessage(
        UUID emailLogId,
        EmailEventType eventType,
        String recipientEmail,
        String recipientName,
        String subject,
        String templateName,
        String link,
        Map<String, Object> templateVars
) {
}
