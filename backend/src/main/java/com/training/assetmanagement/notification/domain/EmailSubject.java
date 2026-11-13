package com.training.assetmanagement.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailSubject {

    PASSWORD_RESET("[Quản lý tài sản] Mật khẩu đăng nhập của bạn đã được đặt lại");

    private final String text;
}
