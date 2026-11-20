package com.training.assetmanagement.media.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

// Tệp ảnh đã mã hóa lại - bất biến, đổi ảnh là tải tệp mới.
// object_key không bao giờ ra API
@Entity
@Table(name = "image_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFile implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "object_key", nullable = false, updatable = false)
    private String objectKey;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Id do ứng dụng sinh: save() gọi persist, không SELECT trước
    @Transient
    @Getter(AccessLevel.NONE)
    private boolean fresh = true;

    public static ImageFile create(UUID id, String objectKey, UUID actorId, LocalDateTime now) {
        ImageFile file = new ImageFile();
        file.id = id;
        file.objectKey = objectKey;
        file.createdBy = actorId;
        file.createdAt = now;
        return file;
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
