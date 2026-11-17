package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

// Một nút cây đã tính sẵn depth, trưởng, loại con và thao tác được phép - service trả cho web
@Schema(name = "OrgUnitView", description = "A view of an organizational unit with its details and allowed actions")
public record OrgUnitView(
    UUID id,
    String code,
    String name,
    OrgUnitType unitType,
    OrgUnitStatus status,
    UUID parentId,
    int depth,
    OrgUnitHead head,
    OrgUnitType childUnitType,
    List<OrgUnitAction> allowedActions
) {
}
