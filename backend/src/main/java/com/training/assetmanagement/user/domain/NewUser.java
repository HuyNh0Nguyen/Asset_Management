package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "NewUser", description = "Represents a new user being created in the system")
public record NewUser(String username, String email, String fullName, String phoneNumber,
                      UUID orgUnitId, UUID rankId, Set<UUID> roleIds) {
}
