package com.training.assetmanagement.organization.domain;

import com.training.assetmanagement.shared.web.ErrorCode;

import java.util.*;

// Quy tắc thuần của cây đơn vị.
// allowedActions và service chặn lệnh gọi CÙNG hàm check()
public final class OrgUnitRules {

    // Loại đơn vị con hợp lệ duy nhất của một loại
    public static OrgUnitType childTypeOf(OrgUnitType parentType) {
        return parentType == OrgUnitType.COMPANY ? OrgUnitType.BRANCH : OrgUnitType.DEPARTMENT;
    }

    // COMPANY chỉ sinh ở seed
    public static Optional<ErrorCode> checkCreatableType(OrgUnitType unitType) {
        return unitType == OrgUnitType.COMPANY
            ? Optional.of(ErrorCode.ORG_UNIT_COMPANY_NOT_ALLOWED)
            : Optional.empty();
    }

    // BRANCH dưới COMPANY; DEPARTMENT dưới BRANCH hoặc DEPARTMENT
    public static Optional<ErrorCode> checkParentType(OrgUnitType unitType, OrgUnitType parentType) {
        return childTypeOf(parentType) == unitType
            ? Optional.empty()
            : Optional.of(ErrorCode.ORG_UNIT_INVALID_PARENT_TYPE);
    }

    // Cha mới không được là chính nó hoặc con cháu của nó
    public static Optional<ErrorCode> checkNoCycle(OrgTree tree, UUID unitId, UUID newParentId) {
        return tree.isInSubtree(newParentId, unitId)
            ? Optional.of(ErrorCode.ORG_UNIT_PARENT_CYCLE)
            : Optional.empty();
    }

    // Không tính vướng ở đây (mỗi nút sẽ tốn nhiều truy vấn) - vướng nằm ở endpoint ...-check.
    // Chỉ tra cây để lấy cha của nút: Nút vừa tạo (chưa có trong cây) vẫn dùng được
    public static Optional<ErrorCode> check(OrgUnitAction action, OrgNode node, OrgTree tree, boolean canManage) {
        if (!canManage) {
            return Optional.of(ErrorCode.FORBIDDEN);
        }
        return switch (action) {
            case ADD_CHILD -> node.isActive() ? Optional.empty() : Optional.of(ErrorCode.ORG_UNIT_PARENT_INACTIVE);
            case EDIT -> Optional.empty();
            case MOVE -> checkMovable(node);
            case DELETE -> node.isRoot() ? Optional.of(ErrorCode.ORG_UNIT_ROOT_IMMUTABLE) : Optional.empty();
            case DEACTIVATE -> checkDeactivatable(node);
            case REACTIVATE -> checkReactivatable(node, tree);
        };
    }

    public static List<OrgUnitAction> allowedActions(OrgNode node, OrgTree tree, boolean canManage) {
        if (!canManage) {
            return List.of();
        }
        return Arrays.stream(OrgUnitAction.values())
            .filter(action -> check(action, node, tree, true).isEmpty())
            .toList();
    }

    // Tổ tiên cũ không còn là tổ tiên sau khi chuyển; sắp id tăng dần để khóa theo cùng thứ tự
    public static List<UUID> detachedAncestorIds(OrgTree tree, UUID unitId, UUID newParentId) {
        Set<UUID> kept = new HashSet<>();
        kept.add(newParentId);
        tree.ancestors(newParentId).forEach(ancestor -> kept.add(ancestor.id()));
        return tree.ancestors(unitId).stream()
            .map(OrgNode::id)
            .filter(id -> !kept.contains(id))
            .sorted()
            .toList();
    }

    // Đơn vị có trưởng đang ngồi trong cây con bị chuyển -> chặn đổi cha
    // headByUnit: Đơn vị -> trưởng; seatByUser: Người -> đơn vị công tác
    public static List<UUID> unitsWithDetachedHead(Map<UUID, UUID> headByUnit, Map<UUID, UUID> seatByUser,
                                                   Set<UUID> movedSubTree) {
        return headByUnit.entrySet().stream()
            .filter(entry -> movedSubTree.contains(seatByUser.get(entry.getValue())))
            .map(Map.Entry::getKey)
            .toList();
    }

    // Chỉ DEPARTMENT đổi cha được: Cha của BRANCH buộc là COMPANY duy nhất
    private static Optional<ErrorCode> checkMovable(OrgNode node) {
        if (node.isRoot()) {
            return Optional.of(ErrorCode.ORG_UNIT_ROOT_IMMUTABLE);
        }
        return node.unitType() == OrgUnitType.DEPARTMENT
            ? Optional.empty()
            : Optional.of(ErrorCode.ORG_UNIT_INVALID_PARENT_TYPE);
    }

    private static Optional<ErrorCode> checkDeactivatable(OrgNode node) {
        if (node.isRoot()) {
            return Optional.of(ErrorCode.ORG_UNIT_ROOT_IMMUTABLE);
        }
        return node.isActive() ? Optional.empty() : Optional.of(ErrorCode.CONFLICT);
    }

    // Kích hoạt lại đi từ trên xuống: Cha phải ACTIVE
    private static Optional<ErrorCode> checkReactivatable(OrgNode node, OrgTree tree) {
        if (node.isActive()) {
            return Optional.of(ErrorCode.CONFLICT);
        }
        boolean parentActive = tree.find(node.parentId()).map(OrgNode::isActive).orElse(false);
        return parentActive ? Optional.empty() : Optional.of(ErrorCode.ORG_UNIT_PARENT_INACTIVE);
    }

    private OrgUnitRules() {}
}
