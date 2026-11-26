package com.training.assetmanagement.user.service;

import java.util.UUID;

// Phần ghi CSDL - BEAN RIÊNG để proxy @Transactional chạy khi AvatarServiceImpl gọi
public interface AvatarWriter {

    // Khóa users, đăng ký ảnh mới, gắn FK, xóa ảnh cũ (tệp sau commit), ghi Audit
    UUID attach(UUID userId, String objectKey, String originalFilename);

    // Chưa có ảnh -> không làm gì, không Audit
    void detach(UUID userId);
}
