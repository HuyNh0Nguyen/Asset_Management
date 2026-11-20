package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.organization.domain.OrgUnitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "UnitRef", description = "Represents a reference to a unit, which may be used to identify or link to a specific organizational unit within the system")
public record UnitRef(UUID id, String code, String name, OrgUnitStatus status) {
}
