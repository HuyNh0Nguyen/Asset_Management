package com.training.assetmanagement.access.repository;

// Một dòng của truy vấn; permissionCode, scope null khi vai trò chưa có ô quyền nào
public interface GrantRow {

    String getRoleCode();

    String getPermissionCode();

    String getScope();
}
