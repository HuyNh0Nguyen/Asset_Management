package com.training.assetmanagement.notification.repository;

import com.training.assetmanagement.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

// Hai truy vấn của chuông dùng chỉ mục idx_notifications_recipient (recipient_id, is_read, created_at)
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadFalse(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    boolean existsByIdAndRecipientId(UUID id, UUID recipientId);

    // 0 dòng = đã đọc rồi hoặc không phải của mình - service phân biệt bằng existsByIdAndRecipientId
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.id = :id AND n.recipientId = :recipientId AND n.read = false
        """)
    int markRead(@Param("id") UUID id, @Param("recipientId") UUID recipientId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.recipientId = :recipientId AND n.read = false
        """)
    int markAllRead(@Param("recipientId") UUID recipientId);
}
