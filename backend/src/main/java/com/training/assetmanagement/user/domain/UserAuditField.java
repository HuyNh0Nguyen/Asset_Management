package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.audit.domain.AuditField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuditField implements AuditField {

    USERNAME("username"),
    EMAIL("email"),
    FULL_NAME("fullName"),
    PHONE_NUMBER("phoneNumber"),
    ORG_UNIT_ID("orgUnitId"),
    RANK_ID("rankId"),
    // Danh sách mã vai trò đã sắp
    ROLES("roles"),
    STATUS("status"),
    MUST_CHANGE_PASSWORD("mustChangePassword"),
    AVATAR_IMAGE_ID("avatarImageId"),
    ORIGINAL_FILENAME("originalFilename");

    private final String key;
}
