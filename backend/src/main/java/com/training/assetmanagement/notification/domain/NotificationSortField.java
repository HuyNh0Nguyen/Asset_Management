package com.training.assetmanagement.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Thuộc tính sắp xếp cố định của danh sách thông báo
@Getter
@RequiredArgsConstructor
public enum NotificationSortField {

    CREATED_AT("createdAt"),
    ID("id");

    private final String property;
}
