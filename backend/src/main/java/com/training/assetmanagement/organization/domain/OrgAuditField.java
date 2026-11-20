package com.training.assetmanagement.organization.domain;

import com.training.assetmanagement.audit.domain.AuditField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Khóa JSON trong old_value / new_value của Audit cho đơn vị và cấp bậc
@Getter
@RequiredArgsConstructor
public enum OrgAuditField implements AuditField {

    CODE("code"),
    NAME("name"),
    UNIT_TYPE("unitType"),
    PARENT_ID("parentId"),
    HEAD_USER_ID("headUserId"),
    STATUS("status"),
    SENIORITY("seniority");

    private final String key;
}
