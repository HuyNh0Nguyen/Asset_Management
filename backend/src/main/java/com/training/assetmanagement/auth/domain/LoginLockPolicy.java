package com.training.assetmanagement.auth.domain;

import java.time.Duration;
import java.time.LocalDateTime;

// Khóa 5 phút sau 5 lần sai liên tiếp; hàm thuần nhận now
public final class LoginLockPolicy {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    public static boolean isLocked(LocalDateTime lockedUntil, LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    // Đã từng khóa và khóa đã hết -> Bộ đếm phải về 0
    public static boolean isLockExpired(LocalDateTime lockedUntil, LocalDateTime now) {
        return lockedUntil != null && !lockedUntil.isAfter(now);
    }

    // Chạm ngưỡng -> Thời điểm hết khóa; chưa chạm -> null
    public static LocalDateTime lockedUntilAfterFailure(int failedLoginCount, LocalDateTime now) {
        return failedLoginCount >= MAX_FAILED_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
    }

    private LoginLockPolicy() {}
}
