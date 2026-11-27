package com.training.assetmanagement.notification.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// 18 sự kiện có kênh in-app - khớp ck_notifications_event_type
public enum NotificationEventType {

    TASK_ACTIVATED,
    REQUEST_APPROVED,
    REQUEST_REJECTED,
    REQUEST_STOPPED,        // Chỉ in-app
    ASSET_HANDED_OVER,
    ASSET_RECOVERED,
    HOLDER_REQUEST,
    TASK_REASSIGNED,
    TASK_UNRESOLVED,
    RECOVERY_DUE_REMINDER,
    RECOVERY_OVERDUE_DIGEST,
    TASK_OVERDUE_REMINDER,
    TASK_OVERDUE_DIGEST,
    APPROVED_REQUEST_CLOSED,
    RECOVERY_EXTENDED,
    HELD_ASSET_MAINTENANCE,
    REQUEST_STAGE_PASSED,   // Chỉ in-app
    EXTENSION_REJECTED;

    // Bảng kênh dựng một lần: Trùng tên với EmailEventType = sự kiện có cả email
    private static final Map<NotificationEventType, EmailEventType> EMAIL_BY_IN_APP =
        new EnumMap<>(NotificationEventType.class);
    private static final Map<EmailEventType, NotificationEventType> IN_APP_BY_EMAIL =
        new EnumMap<>(EmailEventType.class);

    static {
        Map<String, EmailEventType> emailByName = Arrays.stream(EmailEventType.values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));
        for (NotificationEventType inApp : values()) {
            EmailEventType email = emailByName.get(inApp.name());
            if (email != null) {
                EMAIL_BY_IN_APP.put(inApp, email);
                IN_APP_BY_EMAIL.put(email, inApp);
            }
        }
    }

    // Mã email cùng sự kiện; rỗng = chỉ in-app
    public Optional<EmailEventType> emailCounterpart() {
        return Optional.ofNullable(EMAIL_BY_IN_APP.get(this));
    }

    // Mã in-app cùng sự kiện; rỗng = chỉ email
    public static Optional<NotificationEventType> inAppCounterpartOf(EmailEventType email) {
        return Optional.ofNullable(IN_APP_BY_EMAIL.get(email));
    }
}
