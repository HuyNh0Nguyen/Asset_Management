package com.training.assetmanagement.notification.repository;

import com.training.assetmanagement.notification.domain.EmailLog;
import com.training.assetmanagement.notification.domain.EmailLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    // CAS trạng thái: chỉ đổi khi dòng còn ở trạng thái mong đợi; 0 dòng = đã được chốt nơi khác
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE EmailLog e
        SET e.status = :next, e.lastError = :lastError, e.updatedAt = :updatedAt
        WHERE e.id = :id and e.status = :expected
        """)
    int compareAndSetStatus(@Param("id") UUID id,
                            @Param("expected") EmailLogStatus expected,
                            @Param("next") EmailLogStatus next,
                            @Param("lastError") String lastError,
                            @Param("updatedAt") LocalDateTime updatedAt);
}
