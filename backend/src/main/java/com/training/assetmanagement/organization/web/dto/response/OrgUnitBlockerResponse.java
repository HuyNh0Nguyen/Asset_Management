package com.training.assetmanagement.organization.web.dto.response;

import com.training.assetmanagement.organization.domain.OrgUnitBlockerKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// total đếm đúng; samples tối đa 20 mục
@Schema(name = "OrgUnitBlockerResponse", description = "A response containing a list of organizational unit blockers")
public record OrgUnitBlockerResponse(
    @Schema(requiredMode = REQUIRED) OrgUnitBlockerKind kind,
    @Schema(requiredMode = REQUIRED) long total,
    @Schema(requiredMode = REQUIRED) List<OrgUnitBlockerItemResponse> samples
) {
}
