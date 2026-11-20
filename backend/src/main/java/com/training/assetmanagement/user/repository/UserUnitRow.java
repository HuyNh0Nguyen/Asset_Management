package com.training.assetmanagement.user.repository;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "UserUnitRow", description = "Represents a row in the user unit table, which may contain information about a user's association with a specific organizational unit")
public record UserUnitRow(UUID userId, UUID orgUnitId) {
}
