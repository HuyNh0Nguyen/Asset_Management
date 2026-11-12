package com.training.assetmanagement.user.web.dto.response;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Một quyền hiệu lực = hợp phạm vi qua mọi vai trò
@Schema(name = "PermissionGrantResponse", description = "Response for permission grant operations")
public record PermissionGrantResponse(
    @Schema(requiredMode = REQUIRED) Permission code,
    @Schema(requiredMode = REQUIRED) DataScope scope
) {
}
