package com.training.assetmanagement.organization.web.dto.response;

import com.training.assetmanagement.organization.domain.OrgUnitAction;
import com.training.assetmanagement.organization.domain.OrgUnitStatus;
import com.training.assetmanagement.organization.domain.OrgUnitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Một nút của danh sách phẳng tiền thứ tự - FE dựng lại cây bằng parentId
@Schema(name = "OrgUnitNodeResponse", description = "A node in the flattened organizational unit list, used for reconstructing the tree structure on the front-end")
public record OrgUnitNodeResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    @Schema(requiredMode = REQUIRED) OrgUnitType unitType,
    @Schema(requiredMode = REQUIRED) OrgUnitStatus status,
    @Schema(nullable = true) UUID parentId,
    @Schema(requiredMode = REQUIRED) int depth,
    @Schema(nullable = true) OrgUnitHeadResponse head,
    @Schema(requiredMode = REQUIRED) OrgUnitType childUnitType,
    @Schema(requiredMode = REQUIRED) List<OrgUnitAction> allowedActions
) {
}
