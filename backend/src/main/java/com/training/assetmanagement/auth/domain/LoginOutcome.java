package com.training.assetmanagement.auth.domain;

import java.util.UUID;

// Trả về (không ném) để giao dịch đăng nhập commit bộ đếm + Audit trước khi báo lỗi
public sealed interface LoginOutcome
        permits LoginOutcome.Success, LoginOutcome.InvalidCredentials, LoginOutcome.Locked, LoginOutcome.Disabled {

    record Success(UUID userId, boolean mustChangePassword) implements LoginOutcome {}

    record InvalidCredentials() implements LoginOutcome {}

    record Locked() implements LoginOutcome {}

    record Disabled() implements LoginOutcome {}
}
