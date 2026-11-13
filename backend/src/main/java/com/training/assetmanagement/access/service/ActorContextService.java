package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.domain.RoleGrants;

import java.util.UUID;

public interface ActorContextService {

    // Truy vấn: Vai trò + quyền hiệu lực (hợp phạm vi qua các vai trò)
    RoleGrants loadGrants(UUID userId);

    ActorContext buildContext(UUID userId, String username, UUID orgUnitId, boolean mustChangePassword);
}
