package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Một dòng của cây - nạp bằng projection, không phải entity
@Schema(name = "OrgNode", description = "A node in the organizational tree")
public record OrgNode(
    UUID id,
    String code,
    String name,
    OrgUnitType unitType,
    UUID parentId,
    UUID headUserId,
    OrgUnitStatus status
) {
    public boolean isRoot() {
        return parentId == null;
    }

    public boolean isActive() {
        return status == OrgUnitStatus.ACTIVE;
    }
}
