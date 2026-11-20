package com.training.assetmanagement.access.web.dto.response;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// FE dựng ma trận từ đây - không chép phạm vi hợp lệ sang FE
@Schema(name = "PermissionDefinitionResponse", description = "Permission with its allowed scopes")
public record PermissionDefinitionResponse(
    @Schema(requiredMode = REQUIRED) Permission code,
    @Schema(requiredMode = REQUIRED) List<DataScope> allowedScopes,
    @Schema(requiredMode = REQUIRED) boolean adminOnly
) {
}
