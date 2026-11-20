package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Khai ở organization vì web không được dùng DTO của miền user; hàng Ảnh đại diện sẽ thêm avatarUrl
@Schema(name = "OrgUnitHeadResponse", description = "Response for organizational unit head information")
public record OrgUnitHeadResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String username,
    @Schema(requiredMode = REQUIRED) String fullName,
    @Schema(requiredMode = REQUIRED) String orgUnitName,
    String avatarUrl                                    // URL ký bản thu nhỏ; null khi chưa có ảnh
) {
}
