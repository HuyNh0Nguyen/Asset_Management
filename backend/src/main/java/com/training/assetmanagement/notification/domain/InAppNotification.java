package com.training.assetmanagement.notification.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

// Một thông báo in-app cần tạo; content là câu tiếng Việt hoàn chỉnh dựng lúc tạo
@Schema(name = "InAppNotification", description = "In-app notification to be created; content is a complete Vietnamese sentence constructed at creation")
public record InAppNotification(
    UUID recipientId,
    NotificationEventType eventType,
    String content,
    String link         // Đường dẫn tương đối trong ứng dụng (/requests/...), có thể null
) {

    // Khớp notifications.link VARCHAR(512)
    public static final int LINK_MAX_LENGTH = 512;
    private static final String PATH_PREFIX = "/";
    private static final String PROTOCOL_RELATIVE_PREFIX = "//";

    public InAppNotification {
        Objects.requireNonNull(recipientId, "recipientId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Notification content must not be blank");
        }
        if (link != null && !isInAppPath(link)) {
            throw new IllegalArgumentException("Notification link must be a relative in-app path");
        }
    }

    // Bắt đầu bằng "/" nhưng không phải "//" (URL sang host khác)
    private static boolean isInAppPath(String link) {
        return link.startsWith(PATH_PREFIX)
            && !link.startsWith(PROTOCOL_RELATIVE_PREFIX)
            && link.length() <= LINK_MAX_LENGTH;
    }
}
