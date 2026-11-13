package com.training.assetmanagement.auth.service;

import java.util.UUID;

public interface PasswordService {

    // Gỡ cờ bắt buộc đổi, thu hồi mọi token
    void changePassword(String newPassword);

    // Mật khẩu tạm qua email; Admin không thấy mật khẩu tạm
    void resetPassword(UUID userId);

    // Đẩy token_valid_from lên bây giờ
    void logout();
}
