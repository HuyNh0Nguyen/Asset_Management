package com.training.assetmanagement.organization.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Khóa JSON trong old_value / new_value của Audit cho đơn vị và cấp bậc
@Getter
@RequiredArgsConstructor
public enum OrgAuditField {

    CODE("code"),
    NAME("name"),
    UNIT_TYPE("unitType"),
    PARENT_ID("parentId"),
    HEAD_USER_ID("headUserId"),
    STATUS("status"),
    SENIORITY("seniority");

    private final String key;
}
