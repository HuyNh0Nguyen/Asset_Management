package com.training.assetmanagement.user.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Bắt buộc đơn vị: FE điền sẵn đơn vị cũ khi còn ACTIVE
@Schema(name = "ReactivateUserRequest", description = "Reactivate a user into an active department")
public record ReactivateUserRequest(
    @Schema(requiredMode = REQUIRED)
    @NotNull
    UUID orgUnitId
) {
}
