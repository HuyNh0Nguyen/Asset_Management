package com.training.assetmanagement.notification.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

// Một email cần gửi; emailLogId do publisher cấp khi chèn email_logs
@Schema(name = "EmailNotification", description = "An email notification to be sent")
public record EmailNotification(
    UUID recipientId,
    String recipientEmail,
    String recipientName,
    EmailEventType eventType,
    String subject,
    String link,
    Map<String, Object> templateVars
) {
    // templateVars có thể chứa mật khẩu tạm - không bao giờ in ra log
    @Override
    public String toString() {
        return "EmailNotification[recipientId=" + recipientId + ", eventType=" + eventType + "]";
    }
}
