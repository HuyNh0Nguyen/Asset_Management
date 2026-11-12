package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Menu FE dựng theo permissions - không theo tên vai trò
@Schema(name = "MeResponse", description = "Information for the currently authenticated user")
public record MeResponse(
        @Schema(requiredMode = REQUIRED) UUID id,       // FE cần để so "có phải chính tôi"
        @Schema(requiredMode = REQUIRED) String username,
        @Schema(requiredMode = REQUIRED) String fullName,
        @Schema(requiredMode = REQUIRED) String email,
        String avatarUrl,                               // URL ký; null khi chưa có ảnh
        @Schema(requiredMode = REQUIRED) boolean mustChangePassword,
        @Schema(requiredMode = REQUIRED) OrgUnitRefResponse orgUnit,
        @Schema(requiredMode = REQUIRED) String rankName,
        @Schema(requiredMode = REQUIRED) List<PermissionGrantResponse> permissions
) {
}
