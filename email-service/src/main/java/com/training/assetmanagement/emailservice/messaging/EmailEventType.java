package com.training.assetmanagement.emailservice.messaging;

import java.util.Locale;

public enum EmailEventType {

    TASK_ACTIVATED,
    REQUEST_APPROVED,
    REQUEST_REJECTED,
    ASSET_HANDED_OVER,
    ASSET_RECOVERED,
    HOLDER_REQUEST,
    TASK_REASSIGNED,
    TASK_UNRESOLVED,
    RECOVERY_DUE_REMINDER,
    RECOVERY_OVERDUE_DIGEST,
    TASK_OVERDUE_REMINDER,
    ACCOUNT_CREATED,
    PASSWORD_RESET,
    TASK_OVERDUE_DIGEST,
    APPROVED_REQUEST_CLOSED,
    RECOVERY_EXTENDED,
    HELD_ASSET_MAINTENANCE,
    EXTENSION_REJECTED,
    // Email thử tới địa chỉ bất kỳ (emailLogId = null) - không ghi email_logs nên KHÔNG có trong ck_email_logs_event_type
    EMAIL_PROBE;

    // Tên tệp mẫu: REQUEST_REJECTED -> email/request-rejected.html
    public String templateName() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
