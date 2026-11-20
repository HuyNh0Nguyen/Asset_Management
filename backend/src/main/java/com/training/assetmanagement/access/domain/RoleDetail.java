package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Vai trò kèm ma trân, số người giữ, thao tác được phép
@Schema(name = "RoleDetail", description = "Role with its grants, holder count, and allowed actions")
public record RoleDetail(Role role, List<RoleGrant> grants, long holderCount, List<RoleAction> allowedActions) {
}
