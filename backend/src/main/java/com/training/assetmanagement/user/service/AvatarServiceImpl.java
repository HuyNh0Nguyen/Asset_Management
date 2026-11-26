package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.media.domain.StoredImage;
import com.training.assetmanagement.media.service.ImageProcessor;
import com.training.assetmanagement.media.service.PresignedUrlSigner;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final ImageProcessor imageProcessor;
    private final AvatarWriter avatarWriter;
    private final PresignedUrlSigner presignedUrlSigner;
    private final UserRepository userRepository;
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
        // Kiểm sớm: Không xử lý / tải ảnh cho id không tồn tại
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found: " + userId);
        }
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
            // TRONG giao dịch (bean riêng)
            avatarWriter.attach(userId, stored.objectKey(), StringUtils.getFilename(file.getOriginalFilename()));
        } catch (RuntimeException ex) {
            // Ghi dòng thất bại (VD 404, khóa chéo) -> xóa hai đối tượng vừa tải
            imageProcessor.discard(stored);
            throw ex;
        }
        return presignedUrlSigner.thumbnailUrl(stored.objectKey());
    }
}
