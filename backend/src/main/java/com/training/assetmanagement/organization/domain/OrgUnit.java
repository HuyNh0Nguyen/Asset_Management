package com.training.assetmanagement.organization.domain;

import com.training.assetmanagement.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Đơn vị - mã và loại bất biến; đổi qua phương thức miền, không có setter
@Entity
@Table(name = "org_units")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrgUnit extends BaseEntity {

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    // VARCHAR + CHECK, không phải ENUM của MariaDB - cùng cách với User.status
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "unit_type", nullable = false, updatable = false)
    private OrgUnitType unitType;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "head_user_id")
    private UUID headUserId;

    // Chỉ đổi bằng CAS ở OrgUnitRepository.updateStatus
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false)
    private OrgUnitStatus status;

    private OrgUnit(UUID id, String code, String name, OrgUnitType unitType, UUID parentId) {
        super(id);
        this.code = code;
        this.name = name;
        this.unitType = unitType;
        this.parentId = parentId;
        this.status = OrgUnitStatus.ACTIVE;
    }

    // Đơn vị mới: ACTIVE, chưa có trưởng
    public static OrgUnit create(UUID id, String code, String name, OrgUnitType unitType, UUID parentId,
                                 UUID actorId, LocalDateTime now) {
        OrgUnit unit = new OrgUnit(id, code, name, unitType, parentId);
        unit.markCreated(actorId, now);
        return unit;
    }

    public void rename(String name) {
        this.name = name;
    }

    // null = để trống trưởng đơn vị
     public void assignHead(UUID headUserId) {
        this.headUserId = headUserId;
     }

     public void moveUnder(UUID parentId) {
        this.parentId = parentId;
     }

     public OrgNode toNode() {
        return new OrgNode(getId(), code, name, unitType, parentId, headUserId, status);
     }
}
