package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Đơn vị cấp trên cũ có trưởng đang ngồi trong cây con bị chuyển
@Schema(name = "BlockingHead", description = "Old parent unit with a head that is currently in the subtree being moved")
public record BlockingHead(UUID unitId, String code, String name, OrgUnitHead head) {
}
