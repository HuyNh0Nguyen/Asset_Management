package com.training.assetmanagement.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

// Tên thuộc tính JPA của User - dùng cho Specification và tham số sort
@Getter
@RequiredArgsConstructor
public enum UserAttribute {

    ID("id", false),
    USERNAME("username", true),
    EMAIL("email", false),
    FULL_NAME("fullName", true),
    ORG_UNIT_ID("orgUnitId", false),
    STATUS("status", false),
    CREATED_AT("createdAt", true);

    private final String path;
    private final boolean sortable;

    // sort chỉ nhận username / fullName / createdAt
    public static Optional<UserAttribute> sortableByPath(String path) {
        return Arrays.stream(values()).filter(item -> item.sortable && item.path.equals(path)).findFirst();
    }
}
