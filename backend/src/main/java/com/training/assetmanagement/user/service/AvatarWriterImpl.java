package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.media.service.ImageFileService;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAuditField;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

// Khóa ghi dòng người dùng là câu đầu tiên ("khóa dòng chủ"); được cả với tài khoản INACTIVE
@Service
@RequiredArgsConstructor
public class AvatarWriterImpl implements AvatarWriter {

    private final UserRepository userRepository;
    private final ImageFileService imageFileService;
    private final UserAuditService userAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional
    public UUID attach(UUID userId, String objectKey, String originalFilename) {
        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        UUID oldImageId = user.getAvatarImageId();
        String username = user.getUsername();

        UUID newImageId = imageFileService.register(objectKey);
        userRepository.updateAvatar(userId, newImageId, currentActor.get().userId(), LocalDateTime.now(clock));
        // Dòng cũ không còn ai trỏ tới -> xóa; tệp MinIO cũ xóa sau commit
        if (oldImageId != null) {
            imageFileService.delete(oldImageId);
        }

        // Tên tệp gốc chỉ ghi lúc thêm
        AuditChanges changes = new AuditChanges();
        if (oldImageId != null) {
            changes.putOld(UserAuditField.AVATAR_IMAGE_ID, oldImageId);
        }
        changes.putNew(UserAuditField.AVATAR_IMAGE_ID, newImageId)
            .putNew(UserAuditField.ORIGINAL_FILENAME, originalFilename);
        userAuditService.log(AuditAction.IMAGE_ADD, userId, username, changes);
        return newImageId;
    }

    @Override
    @Transactional
    public void detach(UUID userId) {
        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        UUID oldImageId = user.getAvatarImageId();
        // Chưa có ảnh -> trả về, không Audit
        if (oldImageId == null) {
            return;
        }
        String username = user.getUsername();
        userRepository.updateAvatar(userId, null, currentActor.get().userId(), LocalDateTime.now(clock));
        imageFileService.delete(oldImageId);
        userAuditService.log(AuditAction.IMAGE_REMOVE, userId, username, new AuditChanges()
            .putOld(UserAuditField.AVATAR_IMAGE_ID, oldImageId));
    }
}
