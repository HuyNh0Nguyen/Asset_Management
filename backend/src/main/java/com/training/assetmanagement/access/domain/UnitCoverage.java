package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

// Tập đơn vị một quyền phủ. Bản trong bộ nhớ - kiểm một đối tượng, chuẩn đối chiếu cho câu truy vấn
@Schema(name = "UnitCoverage", description = "A set of units covered by a permission. In-memory representation for checking an object, reference for queries")
public record UnitCoverage(boolean all, Set<UUID> unitIds, UUID selfUserId) {

    // Không có quyền
    public static final UnitCoverage NONE = new UnitCoverage(false, Set.of(), null);

    public boolean covers(UUID unitId) {
        return all || (unitId != null && unitIds.contains(unitId));
    }

    public boolean isSelf(Collection<UUID> userIds) {
        return selfUserId != null && userIds.contains(selfUserId);
    }

    // Xem: Đơn vị quản lý ∨ đơn vị sử dụng ∨ SELF
    public boolean matchesView(ScopeTarget target) {
        return covers(target.managingUnitId())
            || target.usingUnitIds().stream().anyMatch(this::covers)
            || isSelf(target.selfUserIds());
    }

    // Tác động: Chỉ đơn vị quản lý; SELF chỉ khi tài nguyên khai selfAppliesToAct
    public boolean matchesAct(ScopeTarget target) {
        return covers(target.managingUnitId()) || (target.selfAppliesToAct() && isSelf(target.selfUserIds()));
    }
}
