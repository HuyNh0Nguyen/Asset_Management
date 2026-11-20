package com.training.assetmanagement.access.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

// access không đọc bảng users - cùng khuôn OrgUnitMemberDirectory
public interface AccessMemberDirectory {

    // Chỉ người ACTIVE: userId -> orgUnitId
    Map<UUID, UUID> activeOrgUnits(Collection<UUID> userIds);
}
