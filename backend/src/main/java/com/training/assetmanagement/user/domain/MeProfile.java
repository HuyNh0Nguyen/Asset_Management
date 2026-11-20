package com.training.assetmanagement.user.domain;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.RoleRef;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Dữ liệu /api/me ở tầng service - controller đổi sang MeResponse
@Schema(name = "MeProfile", description = "Current user profile information")
public record MeProfile(
    UUID id,
    String username,
    String fullName,
    String email,
    String phoneNumber,
    String avatarUrl,
    boolean mustChangePassword,
    UnitRef orgUnit,
    String rankName,
    List<RoleRef> roles,
    Map<Permission, DataScope> grants
) {
}
