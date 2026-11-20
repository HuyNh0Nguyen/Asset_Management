package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Rỗng -> không cần chọn trưởng thay
@Schema(name = "HeadReplacementCheckResponse", description = "Units that need a new head after the unit change")
public record HeadReplacementCheckResponse(
    @Schema(requiredMode = REQUIRED) List<OrgUnitRefResponse> units
) {
}
