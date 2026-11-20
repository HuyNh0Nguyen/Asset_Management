package com.training.assetmanagement.access.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Tên ràng buộc CSDL đổi thành mã lỗi nghiệp vụ
@Getter
@RequiredArgsConstructor
public enum RoleConstraint {

    UK_ROLES_CODE("uk_roles_code"),
    FK_USER_ROLES_ROLE("fk_user_roles_role"),
    FK_WORKFLOW_APPROVER_RULES_ROLE("fk_workflow_approver_rules_role");

    private final String constraintName;
}
