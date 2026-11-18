package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// blockers rỗng = làm được
@Schema(name = "OrgUnitBlockersResponse", description = "A response containing a list of blockers for an organizational unit")
public record OrgUnitBlockersResponse(
    @Schema(requiredMode = REQUIRED) List<OrgUnitBlockerResponse> blockers
) {
}
