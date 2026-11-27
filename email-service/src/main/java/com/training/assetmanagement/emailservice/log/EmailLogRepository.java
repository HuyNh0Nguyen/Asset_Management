package com.training.assetmanagement.emailservice.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

// Mọi lệnh ghi là CAS: 0 dòng = đã được chốt / đổi ở nơi khác
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    // Chốt trạng thái, giữ nguyên last_error của các lần lỗi trước
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE EmailLog e
        SET e.status = :next, e.updatedAt = :updatedAt
        WHERE e.id = :id AND e.status = :expected
        """)
    int compareAndSetStatus(@Param("id") UUID id,
                            @Param("expected") EmailLogStatus expected,
                            @Param("next") EmailLogStatus next,
                            @Param("updatedAt") LocalDateTime updatedAt);

    // Chốt trạng thái kèm lý do
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE EmailLog e
        SET e.status = :next, e.lastError = :lastError, e.updatedAt = :updatedAt
        WHERE e.id = :id AND e.status = :expected
        """)
    int compareAndSetStatusWithError(@Param("id") UUID id,
                                     @Param("expected") EmailLogStatus expected,
                                     @Param("next") EmailLogStatus next,
                                     @Param("lastError") String lastError,
                                     @Param("updatedAt") LocalDateTime updatedAt);

    // Tăng số lần thử lại chỉ khi còn PENDING và chưa ai tăng trước (message giao lại không đếm hai lần)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE EmailLog e
        SET e.attemptCount = e.attemptCount + 1, e.lastError = :lastError, e.updatedAt = :updatedAt
        WHERE e.id = :id AND e.status = :status AND e.attemptCount = :seenAttempt
        """)
    int incrementAttempt(@Param("id") UUID id,
                         @Param("status") EmailLogStatus status,
                         @Param("seenAttempt") int seenAttempt,
                         @Param("lastError") String lastError,
                         @Param("updatedAt") LocalDateTime updatedAt);
}
