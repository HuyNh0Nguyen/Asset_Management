package com.training.assetmanagement.notification.domain;

import java.util.Map;
import java.util.UUID;

public record EmailMessage(
        UUID emailLogId,        // Phase 0 để null; từ phase 2 bắt buộc (trỏ dòng email_logs PENDING)
        EmailEventType eventType,
        String recipientEmail,
        String recipientName,
        String subject,
        String templateName,
        String link,            // Dựng từ app.public-base-url - email-service không cần biết URL giao diện
        Map<String, Object> templateVars
) {
}
