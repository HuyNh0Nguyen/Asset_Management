package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.media.service.ImageFileService;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAction;
import com.training.assetmanagement.user.domain.UserAuditField;
import com.training.assetmanagement.user.domain.UserRules;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

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
        // Câu đầu tiên là khóa: Hai lần đổi ảnh đồng thời xếp hàng
        User user = lockUser(userId);
        UserRules.require(UserAction.CHANGE_AVATAR, user.getStatus());
        UUID oldImageId = user.getAvatarImageId();

        UUID newImageId = imageFileService.register(objectKey);
        userRepository.updateAvatar(userId, newImageId, currentActor.get().userId(), LocalDateTime.now(clock));
        if (oldImageId != null) {
            // Dòng xóa ngay, tệp MinIO xóa SAU commit
            imageFileService.delete(oldImageId);
        }

        // IMAGE_ADD: new = {avatarImageId mới, originalFilename}
        AuditChanges changes = new AuditChanges()
            .putNew(UserAuditField.AVATAR_IMAGE_ID, newImageId)
            .putNew(UserAuditField.ORIGINAL_FILENAME, originalFilename);
        // Đổi lần đầu không có ảnh cũ -> old_value NULL
        if (oldImageId != null) {
            changes.putOld(UserAuditField.AVATAR_IMAGE_ID, oldImageId);
        }
        userAuditService.log(AuditAction.IMAGE_ADD, userId, user.getUsername(), changes);
        return newImageId;
    }

    @Override
    @Transactional
    public void detach(UUID userId) {
        User user = lockUser(userId);
        UUID oldImageId = user.getAvatarImageId();
        if (oldImageId == null) {
            // Chưa có ảnh -> không ghi, không Audit
            return;
        }
        UserRules.require(UserAction.REMOVE_AVATAR, user.getStatus());
        userRepository.updateAvatar(userId, null, currentActor.get().userId(), LocalDateTime.now(clock));
        imageFileService.delete(oldImageId);

        // IMAGE_REMOVE: Chỉ old = {avatarImageId cũ}; new rỗng -> AuditLogger ghi NULL
        userAuditService.log(AuditAction.IMAGE_REMOVE, userId, user.getUsername(),
            new AuditChanges().putOld(UserAuditField.AVATAR_IMAGE_ID, oldImageId));
    }

    private User lockUser(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found: " + userId));
    }
}
