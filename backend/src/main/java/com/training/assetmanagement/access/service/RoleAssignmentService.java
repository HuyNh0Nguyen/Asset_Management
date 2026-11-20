package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.RoleRef;

import java.util.*;

// Cửa duy nhất của miền khác vào user_roles
public interface RoleAssignmentService {

    // Thiếu một id -> 404
    List<RoleRef> requireRoles(Collection<UUID> roleIds);

    // MANDATORY
    void assign(UUID userId, Collection<UUID> roleIds);

    // MANDATORY - chèn / xóa theo phần chênh
    void replaceRoles(UUID userId, Collection<UUID> roleIds);

    List<RoleRef> rolesOf(UUID userId);

    Map<UUID, List<RoleRef>> rolesOfUsers(Collection<UUID> userIds);

    List<RoleRef> rolesByCodes(Collection<String> codes);

    Set<UUID> userIdsWithRole(UUID roleId);
}
