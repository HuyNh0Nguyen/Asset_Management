package com.training.assetmanagement.access.web.mapper;

import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.Role;
import com.training.assetmanagement.access.domain.RoleDetail;
import com.training.assetmanagement.access.domain.RoleGrant;
import com.training.assetmanagement.access.web.dto.response.PermissionDefinitionResponse;
import com.training.assetmanagement.access.web.dto.response.RoleGrantResponse;
import com.training.assetmanagement.access.web.dto.response.RoleResponse;

public final class RoleResponseMapper {

    public static RoleResponse toResponse(RoleDetail detail) {
        Role role = detail.role();
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(),
            role.isSystem(), role.isDefaultForNewUser(), detail.holderCount(),
            detail.grants().stream().map(RoleResponseMapper::toGrant).toList(), detail.allowedActions());
    }

    public static PermissionDefinitionResponse toDefinition(Permission permission) {
        return new PermissionDefinitionResponse(permission, permission.allowedScopes(), permission.isAdminOnly());
    }

    private static RoleGrantResponse toGrant(RoleGrant grant) {
        return new RoleGrantResponse(grant.permission(), grant.scope());
    }

    private RoleResponseMapper() {}
}
