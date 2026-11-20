package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "UserDeactivationCheckResponse", description = "Blockers and headed units before deactivation")
public record UserDeactivationCheckResponse(
    @Schema(requiredMode = REQUIRED) List<UserBlockerResponse> blockers,
    @Schema(requiredMode = REQUIRED) List<OrgUnitRefResponse> headedUnits
) {
}
