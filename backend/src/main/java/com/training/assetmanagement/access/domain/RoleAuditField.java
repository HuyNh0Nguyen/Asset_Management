package com.training.assetmanagement.access.domain;

import com.training.assetmanagement.audit.domain.AuditField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleAuditField implements AuditField {

    CODE("code"),
    NAME("name"),
    DESCRIPTION("description"),
    // Toàn bộ ma trận cũ -> mới
    PERMISSIONS("permissions");

    private final String key;
}
