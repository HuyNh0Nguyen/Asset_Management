package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "OrgUnitBlockerItemResponse", description = "A response object representing a single item that blocks an organizational unit from being deactivated or deleted")
public record OrgUnitBlockerItemResponse(UUID id, String code, String name, boolean inactive) {
}
