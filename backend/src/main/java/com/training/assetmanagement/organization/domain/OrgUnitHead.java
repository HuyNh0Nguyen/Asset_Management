package com.training.assetmanagement.organization.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Trưởng đơn vị / ứng viên trưởng để hiển thị
@Schema(name = "OrgUnitHead", description = "Organization Unit Head")
public record OrgUnitHead(UUID id, String username, String fullName, String orgUnitName) {
}
