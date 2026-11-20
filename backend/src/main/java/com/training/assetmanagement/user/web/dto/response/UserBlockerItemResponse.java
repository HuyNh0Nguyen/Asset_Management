package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "UserBlockerItemResponse", description = "One sample item blocking deactivation")
public record UserBlockerItemResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    String detail                                   // Số lượng đang giữ / tên bước duyệt
) {
}
