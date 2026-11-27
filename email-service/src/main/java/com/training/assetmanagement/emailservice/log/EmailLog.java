package com.training.assetmanagement.emailservice.log;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Bảng của backend: email-service chỉ đọc và chốt bằng CAS, không bao giờ chèn.
// Chỉ ánh xạ các cột cần đọc / ghi - cột khác không có trong entity
@Entity
@Table(name = "email_logs")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLog {

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false)
    private EmailLogStatus status;

    // Số lần thử lại đã lên lịch (0..maxRetries)
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
