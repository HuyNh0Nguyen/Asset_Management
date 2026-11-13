package com.training.assetmanagement.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Tên biến trong mẫu Thymeleaf - phải khớp templates/email/*.html
@Getter
@RequiredArgsConstructor
public enum EmailTemplateVar {

    USERNAME("username"),
    TEMPORARY_PASSWORD("temporaryPassword"),
    // Nhãn nút trong base-layout; vắng mặt -> "Mở hệ thống"
    LINK_LABEL("linkLabel");

    private final String key;
}
