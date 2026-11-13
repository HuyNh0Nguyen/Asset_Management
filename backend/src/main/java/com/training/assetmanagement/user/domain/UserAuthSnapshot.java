package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

// Truy vấn mỗi request: Một dòng users theo khóa chính
@Schema(name = "UserAuthScapshot", description = "Snapshot of user authentication information")
public record UserAuthSnapshot(
    UUID id,
    String username,
    UserStatus status,
    LocalDateTime tokenValidFrom,
    boolean mustChangePassword,
    UUID orgUnitId
) {
}
