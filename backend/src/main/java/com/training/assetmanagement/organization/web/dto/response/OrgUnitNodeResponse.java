package com.training.assetmanagement.organization.web.dto.response;

import com.training.assetmanagement.organization.domain.OrgUnitAction;
import com.training.assetmanagement.organization.domain.OrgUnitStatus;
import com.training.assetmanagement.organization.domain.OrgUnitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

// Một nút của danh sách phẳng tiền thứ tự - FE dựng lại cây bằng parentId
@Schema(name = "OrgUnitNodeResponse", description = "A node in the flattened organizational unit list, used for reconstructing the tree structure on the front-end")
public record OrgUnitNodeResponse(
    UUID id,
    String code,
    String name,
    OrgUnitType unitType,
    OrgUnitStatus status,
    @Schema(nullable = true) UUID parentId,
    int depth,
    @Schema(nullable = true) OrgUnitHeadResponse head,
    OrgUnitType childUnitType,
    List<OrgUnitAction> allowedActions
) {
}
