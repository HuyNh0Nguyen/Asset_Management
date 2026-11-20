package com.training.assetmanagement.access.domain;

import com.training.assetmanagement.organization.domain.OrgNode;
import com.training.assetmanagement.organization.domain.OrgTree;

import java.util.Set;
import java.util.UUID;

// Phạm vi hiệu lực -> tập đơn vị phủ. Thuần; chiều xuôi và chiều ngược cùng gọi.
// Hợp qua các vai trò = phạm vi rộng nhất (DataScope.widest) vì bốn phạm vi lồng nhau và cùng neo
// vào đơn vị người dùng. Thêm phạm vi không lồng (VD "đơn vị chỉ định") thì phải đổi sang hợp tập.
// Nút INACTIVE nằm trong tập - tra cứu vẫn thấy người đã nghỉ của đơn vị giải thể
public final class ScopeResolver {

    public static UnitCoverage resolve(DataScope scope, UUID actorId, UUID actorUnitId, OrgTree tree) {
        if (scope == null) {
            return UnitCoverage.NONE;
        }
        return switch (scope) {
            case SELF -> new UnitCoverage(false, Set.of(), actorId);
            case DEPARTMENT -> new UnitCoverage(false, Set.copyOf(tree.subtreeIds(actorUnitId)), actorId);
            case BRANCH -> new UnitCoverage(false, Set.copyOf(tree.subtreeIds(branchOf(actorUnitId, tree))), actorId);
            case ALL -> new UnitCoverage(true, Set.of(), actorId);
        };
    }

    // Không thuộc chi nhánh nào chỉ có thể do SQL tay - không âm thầm hạ xuống DEPARTMENT
    private static UUID branchOf(UUID unitId, OrgTree tree) {
        return tree.branchOf(unitId).map(OrgNode::id)
            .orElseThrow(() -> new IllegalStateException("Org unit " + unitId + " is not inside a branch"));
    }

    private ScopeResolver() {}
}
