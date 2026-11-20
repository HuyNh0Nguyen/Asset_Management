package com.training.assetmanagement.user.service;

import java.util.UUID;

// Phần ghi CSDL của đổi / gỡ ảnh - bean riêng: Gọi nội bộ trong cùng bean thì proxy giao dịch không chạy
public interface AvatarWriter {

    // Trả id image_files mới
    UUID attach(UUID userId, String objectKey, String originalFilename);

    void detach(UUID userId);
}
