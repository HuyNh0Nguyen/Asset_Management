package com.training.assetmanagement.access.web.dto.response;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "RoleGrantResponse", description = "Granted permission with its scope")
public record RoleGrantResponse(
    @Schema(requiredMode = REQUIRED) Permission permission,
    @Schema(requiredMode = REQUIRED) DataScope scope
) {
}
