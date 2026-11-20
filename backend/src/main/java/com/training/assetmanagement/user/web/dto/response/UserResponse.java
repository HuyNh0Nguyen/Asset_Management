package com.training.assetmanagement.user.web.dto.response;

import com.training.assetmanagement.user.domain.UserAction;
import com.training.assetmanagement.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Một DTO cho cả danh sách lẫn chi tiết; menu dòng FE dựng chỉ từ allowedActions
@Schema(name = "UserResponse", description = "User account")
public record UserResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String username,
    @Schema(requiredMode = REQUIRED) String email,
    @Schema(requiredMode = REQUIRED) String fullName,
    String phoneNumber,
    String avatarUrl,                               // URL ký bản thu nhỏ; null khi chưa có ảnh
    @Schema(requiredMode = REQUIRED) OrgUnitRefResponse orgUnit,
    @Schema(requiredMode = REQUIRED) RankRefResponse rank,
    @Schema(requiredMode = REQUIRED) List<RoleRefResponse> roles,
    @Schema(requiredMode = REQUIRED) UserStatus status,
    @Schema(requiredMode = REQUIRED) List<UserAction> allowedActions
) {
}
