package com.training.assetmanagement.organization.web.dto.response;

import com.training.assetmanagement.organization.domain.OrgUnitBlockerKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// total đếm đúng; samples tối đa 20 mục
@Schema(name = "OrgUnitBlockerResponse", description = "A response containing a list of organizational unit blockers")
public record OrgUnitBlockerResponse(OrgUnitBlockerKind kind, long total, List<OrgUnitBlockerItemResponse> samples) {
}
