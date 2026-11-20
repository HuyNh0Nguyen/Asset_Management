package com.training.assetmanagement.access.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

// 19 mã, đúng thứ tự CHECK ck_role_permissions_code.
// Phạm vi hợp lệ khớp từng ô với ck_role_permissions_self_only / _all_only
@Getter
@RequiredArgsConstructor
public enum Permission {
    ASSET_VIEW(ScopeRule.ANY, false),
    ASSET_CREATE(ScopeRule.ANY, false),
    ASSET_TECH_EDIT(ScopeRule.ANY, false),
    ASSET_FINANCE_VIEW(ScopeRule.ANY, false),
    ASSET_FINANCE_EDIT(ScopeRule.ANY, false),
    ASSET_OPERATE(ScopeRule.ANY, false),
    ASSET_TRANSFER(ScopeRule.ANY, false),
    DISPOSAL_PROPOSE(ScopeRule.ANY, false),
    REQUEST_CREATE(ScopeRule.SELF_ONLY, false),
    REQUEST_VIEW(ScopeRule.ANY, false),
    CATALOG_MANAGE(ScopeRule.ALL_ONLY, false),
    DASHBOARD_VIEW(ScopeRule.ANY, false),
    WORKFLOW_MANAGE(ScopeRule.ALL_ONLY, false),
    TASK_REASSIGN(ScopeRule.ALL_ONLY, false),
    ORG_MANAGE(ScopeRule.ALL_ONLY, false),
    // Chỉ vai trò ADMIN được mang - kiểm ở RoleRules
    USER_MANAGE(ScopeRule.ALL_ONLY, true),
    ROLE_MANAGE(ScopeRule.ALL_ONLY, true),
    AUDIT_VIEW(ScopeRule.ALL_ONLY, false),
    SYSTEM_CONFIG(ScopeRule.ALL_ONLY, false);

    private final ScopeRule scopeRule;
    private final boolean adminOnly;

    // Thứ tự hẹp -> rộng; FE dựng cột ma trận theo đây
    public List<DataScope> allowedScopes() {
        return scopeRule.scopes;
    }

    public boolean allows(DataScope scope) {
        return scope != null && scopeRule.scopes.contains(scope);
    }

    @RequiredArgsConstructor
    private enum ScopeRule {
        SELF_ONLY(List.of(DataScope.SELF)),
        ALL_ONLY(List.of(DataScope.ALL)),
        ANY(List.of(DataScope.SELF, DataScope.DEPARTMENT, DataScope.BRANCH, DataScope.ALL));

        private final List<DataScope> scopes;
    }
}
