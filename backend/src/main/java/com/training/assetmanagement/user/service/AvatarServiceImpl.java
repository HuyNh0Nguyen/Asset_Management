package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.media.domain.StoredImage;
import com.training.assetmanagement.media.service.ImageProcessor;
import com.training.assetmanagement.media.service.PresignedUrlSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// Facade KHÔNG giao dịch: Xử lý ảnh + tải MinIO trước, rồi mới mở giao dịch ghi dòng
@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final ImageProcessor imageProcessor;
    private final AvatarWriter avatarWriter;
    private final PresignedUrlSigner presignedUrlSigner;
    private final CurrentActor currentActor;

    @Override
    public String changeMyAvatar(MultipartFile file) {
        return change(currentActor.get().userId(), file);
    }

    @Override
    public void removeMyAvatar() {
        avatarWriter.detach(currentActor.get().userId());
    }

    @Override
    public String changeAvatar(UUID userId, MultipartFile file) {
        return change(userId, file);
    }

    @Override
    public void removeAvatar(UUID userId) {
        avatarWriter.detach(userId);
    }

    private String change(UUID userId, MultipartFile file) {
        // Ngoài giao dịch
        StoredImage stored = imageProcessor.process(file);
        try {
            // Giao dịch ghi dòng
            avatarWriter.attach(userId, stored.objectKey(), StringUtils.getFilename(file.getOriginalFilename()));
        } catch (RuntimeException ex) {
            // Ghi dòng thất bại (VD 404, khóa chéo) -> xóa hai đối tượng vừa tải
            imageProcessor.discard(stored);
            throw ex;
        }
        // Luôn trả URL ký của bản thu nhỏ; không bao giờ trả objectKey
        return presignedUrlSigner.thumbnailUrl(stored.objectKey());
    }
}
