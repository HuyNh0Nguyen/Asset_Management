package com.training.assetmanagement.access.domain;

import com.training.assetmanagement.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Vai trò - mã bất biến sau khi tạo; ADMIN là vai trò hệ thống
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    private Role(UUID id, String code, String name, String description) {
        super(id);
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static Role create(UUID id, String code, String name, String description,
                              UUID actorId, LocalDateTime now) {
        Role role = new Role(id, code, name, description);
        role.markCreated(actorId, now);
        return role;
    }

    public void rename(String name, String description, UUID actorId, LocalDateTime now) {
        this.name = name;
        this.description = description;
        markUpdated(actorId, now);
    }

    public boolean isSystem() {
        return RoleCode.ADMIN.matches(code);
    }

    public boolean isDefaultForNewUser() {
        return RoleCode.EMPLOYEE.matches(code);
    }
}
