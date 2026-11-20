package com.training.assetmanagement.access.web.dto.response;

import com.training.assetmanagement.access.domain.RoleAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// FE tích sẵn vai trò theo defaultForNewUser - không tự biết mã EMPLOYEE
@Schema(name = "RoleResponse", description = "Role with its permission matrix")
public record RoleResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    String description,
    @Schema(requiredMode = REQUIRED) boolean system,
    @Schema(requiredMode = REQUIRED) boolean defaultForNewUser,
    @Schema(requiredMode = REQUIRED) long holderCount,
    @Schema(requiredMode = REQUIRED) List<RoleGrantResponse> grants,
    @Schema(requiredMode = REQUIRED) List<RoleAction> allowedActions
) {
}
