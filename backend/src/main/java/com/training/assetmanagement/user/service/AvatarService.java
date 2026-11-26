package com.training.assetmanagement.user.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// Facade KHÔNG giao dịch: Xử lý ảnh + tải MinIO không giữ khóa CSDL
public interface AvatarService {

    // Trả URL ký bản thu nhỏ của ảnh mới
    String changeMyAvatar(MultipartFile file);

    void removeMyAvatar();

    String changeAvatar(UUID userId, MultipartFile file);

    void removeAvatar(UUID userId);
}
