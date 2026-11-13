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

// Backend chèn PENDING, chỉ tự chốt FAILED khi không đẩy được lên broker; còn lại email-service chốt
@Entity
@Table(name = "email_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private UUID recipientId;

    @Column(name = "recipient_email", nullable = false, updatable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_type", nullable = false, updatable = false)
    private EmailEventType eventType;

    @Column(name = "subject", nullable = false, updatable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false)
    private EmailLogStatus status;

    // columnDefinition khớp kiểu TEXT để ddl-auto=validate không báo lệch
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Địa chỉ chụp lúc gửi; không lưu nội dung
    public static EmailLog pending(UUID recipientId, String recipientEmail, EmailEventType eventType,
                                   String subject, LocalDateTime now) {
        EmailLog emailLog = new EmailLog();
        emailLog.recipientId = recipientId;
        emailLog.recipientEmail = recipientEmail;
        emailLog.eventType = eventType;
        emailLog.subject = subject;
        emailLog.status = EmailLogStatus.PENDING;
        emailLog.createdAt = now;
        emailLog.updatedAt = now;
        return emailLog;
    }
}
