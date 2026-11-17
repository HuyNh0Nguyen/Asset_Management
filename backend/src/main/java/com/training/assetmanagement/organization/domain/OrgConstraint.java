package com.training.assetmanagement.organization.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Tên ràng buộc duy nhấtở V1 - dùng để đổi vi phạm CSDL thành mã lỗi nghiệp vụ
@Getter
@RequiredArgsConstructor
public enum OrgConstraint {

    UK_ORG_UNITS_CODE("uk_org_units_code"),
    UK_RANKS_CODE("uk_ranks_code"),
    UK_RANKS_SENIORITY("uk_ranks_seniority");

    private final String constraintName;
}
