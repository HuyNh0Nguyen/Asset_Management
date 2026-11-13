package com.training.assetmanagement.user.service;

import com.training.assetmanagement.user.domain.UserAuthSnapshot;
import com.training.assetmanagement.user.domain.UserCredential;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// Nơi DUY NHẤT ghi failed_login_count, locked_until, token_valid_from
public interface UserCredentialService {

    Optional<UUID> findIdByUsername(String username);

    Optional<UserAuthSnapshot> findAuthSnapshot(UUID userId);

    // Khóa dòng users - gọi trong giao dịch của bên gọi
    Optional<UserCredential> lockById(UUID userId);

    void recordFailedLogin(UUID userId, int failedLoginCount, LocalDateTime lockedUntil);

    void clearLoginFailures(UUID userId);

    // Đổi / đặt lại mật khẩu: Đồng thời thu hồi mọi token đang sống
    void updatePassword(UUID userId, String passwordHash, boolean mustChangePassword, UUID updatedBy);

    // Đăng xuất, vô hiệu hóa: Đẩy token_valid_from lên bây giờ
    void revokeTokens(UUID userId);
}
