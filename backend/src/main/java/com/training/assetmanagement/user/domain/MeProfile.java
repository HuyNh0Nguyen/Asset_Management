package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

// Dữ liệu /api/me ở tầng service - controller đổi sang MeResponse
@Schema(name = "MeProfile", description = "Current user profile information")
public record MeProfile(
    UUID id,
    String username,
    String fullName,
    String email,
    boolean mustChangePassword,
    UUID orgUnitId,
    String orgUnitCode,
    String orgUnitName,
    String rankName,
    Map<Permission, DataScope> grants
) {
}
