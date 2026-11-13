package com.training.assetmanagement.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Nhãn nút trong email theo đích của link
@Getter
@RequiredArgsConstructor
public enum EmailLinkLabel {

    LOGIN("Đăng nhập hệ thống");

    private final String text;
}
