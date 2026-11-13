package com.training.assetmanagement.auth.domain;

// Giá trị "reason" trong new_value của Audit LOGIN_FAILURE
public enum LoginFailureReason {
    BAD_PASSWORD,
    LOCKED,
    INACTIVE
}
