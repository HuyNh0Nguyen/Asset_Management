package com.training.assetmanagement.access.service;

import java.util.UUID;

// Luôn còn ≥ 1 người ACTIVE giữ ADMIN
public interface AdminRoleGuard {

    // MANDATORY - câu đầu tiên của Sửa người dùng / Vô hiệu hóa
    UUID lockAdminRole();

    // MANDATORY - gọi sau mọi khóa
    void checkStillHasActiveAdmin(UUID targetUserId);

    boolean holdsAdmin(UUID userId);

    // Chỉ để trình bày ở deactivation-check
    boolean isLastActiveAdmin(UUID userId);
}
