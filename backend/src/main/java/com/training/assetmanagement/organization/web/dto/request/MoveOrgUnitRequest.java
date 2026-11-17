package com.training.assetmanagement.organization.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "MoveOrgUnitRequest", description = "Request to move an organizational unit to a new parent unit")
public record MoveOrgUnitRequest(
    @NotNull
    UUID parentId
) {
}
