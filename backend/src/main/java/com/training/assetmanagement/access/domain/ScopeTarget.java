package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

// Một đối tượng cần kiểm phạm vi: Đơn vị quản lý, các đơn vị sử dụng, người có quan hệ SELF
@Schema(name = "ScopeTarget", description = "An object that requires scope checking: managing unit, using units, or SELF-related users")
public record ScopeTarget(UUID managingUnitId, Set<UUID> usingUnitIds, Set<UUID> selfUserIds,
                          boolean selfAppliesToAct) {

    public static ScopeTarget of(UUID managingUnitId, UUID selfUserId, boolean selfAppliesToAct) {
        return new ScopeTarget(managingUnitId, Set.of(),
            selfUserId == null ? Set.of() : Set.of(selfUserId), selfAppliesToAct);
    }
}
