package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Đơn vị cấp trên cũ có trưởng đang ngồi trong cây con bị chuyển
@Schema(name = "BlockingHeadResponse", description = "Response indicating that the head of the organizational unit is blocking certain operations")
public record BlockingHeadResponse(
    @Schema(requiredMode = REQUIRED) UUID unitId,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    @Schema(requiredMode = REQUIRED) OrgUnitHeadResponse head
) {
}
