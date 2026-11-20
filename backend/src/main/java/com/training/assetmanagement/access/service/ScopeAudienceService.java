package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.Permission;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// TODO: Chiều ngược: Ai (ACTIVE) có quyền P phủ đơn vị U - dùng từ phase 6
public interface ScopeAudienceService {

    Set<UUID> activeUsersCovering(Permission permission, UUID unitId);

    // unitId -> người nhận
    Map<UUID, Set<UUID>> activeUsersCovering(Permission permission, Collection<UUID> unitIds);

    // Quyền chỉ có ALL
    Set<UUID> activeHolders(Permission permission);
}
