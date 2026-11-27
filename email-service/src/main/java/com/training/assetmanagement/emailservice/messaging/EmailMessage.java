package com.training.assetmanagement.emailservice.messaging;

import java.util.Map;
import java.util.UUID;

// Khớp contracts/email-message.example.json
public record EmailMessage(
        UUID emailLogId,                    // Bắt buộc với mọi sự kiện, trừ EMAIL_PROBE tới địa chỉ bất kỳ (null - không ghi email_logs)
        EmailEventType eventType,
        String recipientEmail,
        String recipientName,
        String subject,
        String templateName,
        String link,                        // Backend dựng sẵn link tuyệt đối - email-service không cần biết URL giao diện
        Map<String, Object> templateVars
) {
}
