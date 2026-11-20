package com.training.assetmanagement.access.domain;

import com.training.assetmanagement.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Một ô ma trận quyên; xóa vai trò thì CASCADE ở CSDL
@Entity
@Table(name = "role_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission extends BaseEntity {

    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "permission_code", nullable = false, updatable = false)
    private Permission permission;


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "scope", nullable = false)
    private DataScope scope;

    private RolePermission(UUID id, UUID roleId, Permission permission, DataScope scope) {
        super(id);
        this.roleId = roleId;
        this.permission = permission;
        this.scope = scope;
    }

    public static RolePermission create(UUID id, UUID roleId, RoleGrant grant, UUID actorId, LocalDateTime now) {
        RolePermission cell = new RolePermission(id, roleId, grant.permission(), grant.scope());
        cell.markCreated(actorId, now);
        return cell;
    }

    public void changeScope(DataScope scope, UUID actorId, LocalDateTime now) {
        this.scope = scope;
        markUpdated(actorId, now);
    }

    public RoleGrant toGrant() {
        return new RoleGrant(permission, scope);
    }
}
