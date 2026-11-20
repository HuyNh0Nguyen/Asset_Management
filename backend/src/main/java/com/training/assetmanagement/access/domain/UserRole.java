package com.training.assetmanagement.access.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

// Gán vai trò - gỡ là DELETE nên chỉ có created_*
@Entity
@Table(name = "user_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Id do ứng dụng sinh: save() gọi persist, không SELECT trước - cùng cách BaseEntity
    @Transient
    @Getter(AccessLevel.NONE)
    private boolean fresh = true;

    public static UserRole create(UUID id, UUID userId, UUID roleId, UUID actorId, LocalDateTime now) {
        UserRole link = new UserRole();
        link.id = id;
        link.userId = userId;
        link.roleId = roleId;
        link.createdBy = actorId;
        link.createdAt = now;
        return link;
    }

    @Override
    public boolean isNew() {
        return fresh;
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        this.fresh = false;
    }
}
