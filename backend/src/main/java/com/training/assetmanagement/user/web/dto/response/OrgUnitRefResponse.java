package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "OrgUnitRefResponse", description = "Response for organization unit reference")
public record OrgUnitRefResponse(
        @Schema(requiredMode = REQUIRED) UUID id,       // FE cần: Giá trị mặc định của bộ lọc đơn vị
        @Schema(requiredMode = REQUIRED) String code,
        @Schema(requiredMode = REQUIRED) String name
) {
}
