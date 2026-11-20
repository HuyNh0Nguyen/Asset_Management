package com.training.assetmanagement.access.repository;

import com.training.assetmanagement.access.domain.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// Một dòng (người giữ, phạm vi) của quyền P qua một vai trò
@Schema(name = "HolderGrant", description = "A row (holder, scope) of permission P through a role")
public record HolderGrant(UUID userId, DataScope scope) {
}
