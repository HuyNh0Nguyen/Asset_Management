package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

// Kết quả truy vấn: Mã vai trò (cho audit_logs.actor_roles) + quyền hiệu lực
@Schema(name = "RoleGrants", description = "Role codes and their effective grants")
public record RoleGrants(List<String> roleCodes, Map<Permission, DataScope> grants) {
}
