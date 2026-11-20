package com.training.assetmanagement.access.domain;

import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;

import java.util.Collection;
import java.util.List;

// Quy tắc thuần của vai trò - allowedActions và chặn lệnh gọi cùng dùng
public final class RoleRules {

    // Phạm vi phải nằm trong allowedScopes; quyền adminOnly không vào vai trò thuờng
    public static void checkGrants(Collection<RoleGrant> grants) {
        for (RoleGrant grant : grants) {
            if (!grant.permission().allows(grant.scope())) {
                throw new BusinessException(ErrorCode.ROLE_PERMISSION_SCOPE_INVALID);
            }
            if (grant.permission().isAdminOnly()) {
                throw new BusinessException(ErrorCode.ROLE_PERMISSION_ADMIN_ONLY);
            }
        }
    }

    // ADMIN khóa toàn bộ, kể cả tên / mô tả
    public static void checkEditable(Role role) {
        if (role.isSystem()) {
            throw new BusinessException(ErrorCode.ROLE_SYSTEM_LOCKED);
        }
    }

    // Quy tắc người duyệt vẫn có thể chặn xóa - lệnh thật trả ROLE_IN_USE
    public static List<RoleAction> allowedActions(Role role, long holderCount) {
        if (role.isSystem()) {
            return List.of();
        }
        return holderCount == 0 ? List.of(RoleAction.EDIT, RoleAction.DELETE) : List.of(RoleAction.EDIT);
    }

    private RoleRules() {}
}
