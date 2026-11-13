package com.training.assetmanagement.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Khóa JSON trong old_value / new_value của Audit miền auth
@Getter
@RequiredArgsConstructor
public enum AuthAuditField {

    REASON("reason"),
    FAILED_LOGIN_COUNT("failedLoginCount"),
    LOCKED_UNTIL("lockedUntil"),
    MUST_CHANGE_PASSWORD("mustChangePassword");

    private final String key;
}
