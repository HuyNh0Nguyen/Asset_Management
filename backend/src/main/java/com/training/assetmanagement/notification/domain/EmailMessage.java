package com.training.assetmanagement.notification.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(name = "EmailMessage", description = "Email message to be sent, matching the contract in contracts/email-message.example.json")
public record EmailMessage(
        UUID emailLogId,        // Bắt buộc với mọi sự kiện, trừ EMAIL_PROBE tới địa chỉ bất kỳ (null - không ghi email_logs)
        EmailEventType eventType,
        String recipientEmail,
        String recipientName,
        String subject,
        String templateName,
        String link,            // Dựng từ app.public-base-url - email-service không cần biết URL giao diện
        Map<String, Object> templateVars
) {
}
