package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "OrgUnitBlockerItemResponse", description = "A response object representing a single item that blocks an organizational unit from being deactivated or deleted")
public record OrgUnitBlockerItemResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    @Schema(requiredMode = REQUIRED) boolean inactive
) {
}
