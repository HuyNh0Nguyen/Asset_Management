package com.training.assetmanagement.shared.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

// Khóa chính + 4 cột dấu vết dùng chung
// Không dùng JPA auditing: Người thao tác truyền tường minh từ CurrentActor
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Id do ứng dụng sinh: Đánh dấu mới để save() gọi persist, không SELECT trước
    @Transient
    @Getter(AccessLevel.NONE)
    private boolean fresh = true;

    protected BaseEntity(UUID id) {
        this.id = id;
    }

    public void markCreated(UUID actorId, LocalDateTime now) {
        this.createdBy = actorId;
        this.createdAt = now;
        markUpdated(actorId, now);
    }

    public void markUpdated(UUID actorId, LocalDateTime now) {
        this.updatedBy = actorId;
        this.updatedAt = now;
    }

    @Override
    public boolean isNew() {
        return fresh;
    }

    // Nạp từ CSDL hoặc vừa lưu -> không còn là bản ghi mới
    @PostLoad
    @PostPersist
    void markPersisted() {
        this.fresh = false;
    }
}
