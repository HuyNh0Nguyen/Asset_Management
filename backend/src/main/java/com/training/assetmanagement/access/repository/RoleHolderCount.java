package com.training.assetmanagement.access.repository;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RoleHolderCount", description = "The count of users holding a specific role")
public record RoleHolderCount(UUID roleId, Long total) {
}
