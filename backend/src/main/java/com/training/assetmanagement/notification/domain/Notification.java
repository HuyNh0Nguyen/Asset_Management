package com.training.assetmanagement.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Bảng nhật ký: Không kế thừa BaseEntity; is_read là cột duy nhất đổi được
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_type", nullable = false, updatable = false)
    private NotificationEventType eventType;

    // columnDefinition khớp kiểu TEXT để ddl-auto=validate không báo lệch
    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "text")
    private String content;

    @Column(name = "link", length = InAppNotification.LINK_MAX_LENGTH, updatable = false)
    private String link;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification unread(InAppNotification source, LocalDateTime now) {
        Notification notification = new Notification();
        notification.recipientId = source.recipientId();
        notification.eventType = source.eventType();
        notification.content = source.content();
        notification.link = source.link();
        notification.read = false;
        notification.createdAt = now;
        return notification;
    }
}
