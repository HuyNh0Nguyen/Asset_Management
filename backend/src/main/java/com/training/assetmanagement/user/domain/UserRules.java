package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.organization.domain.OrgUnitType;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

// allowedActions và chặn lệnh gọi cùng một hàm
public final class UserRules {

    // Người dùng chỉ ngồi ở phòng ban
    public static final Set<OrgUnitType> ALLOWED_UNIT_TYPES = Set.of(OrgUnitType.DEPARTMENT);

    public static boolean isAllowed(UserAction action, UserStatus status, boolean hasAvatar) {
        return switch (action) {
            // Tài khoản INACTIVE chỉ đọc, trừ ảnh đại diện và kích hoạt lại
            case EDIT, RESET_PASSWORD, DEACTIVATE -> status == UserStatus.ACTIVE;
            case REACTIVATE -> status == UserStatus.INACTIVE;
            case CHANGE_AVATAR -> true;
            case REMOVE_AVATAR -> hasAvatar;
        };
    }

    // Thứ chặn vô hiệu hóa không tính ở đây (nằm ở deactivation-check)
    public static List<UserAction> allowedActions(UserStatus status, boolean hasAvatar) {
        return Arrays.stream(UserAction.values())
            .filter(action -> isAllowed(action, status, hasAvatar))
            .toList();
    }

    // Sửa tài khoản INACTIVE -> 422; đổi trạng thái sai chiều -> 409
    public static void require(UserAction action, UserStatus status) {
        if (!isAllowed(action, status, true)) {
            throw new BusinessException(action == UserAction.EDIT ? ErrorCode.USER_NOT_ACTIVE : ErrorCode.CONFLICT);
        }
    }

    private UserRules() {}
}
