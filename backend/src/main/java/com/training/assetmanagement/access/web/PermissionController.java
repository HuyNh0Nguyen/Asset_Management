package com.training.assetmanagement.access.web;

import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.web.dto.response.PermissionDefinitionResponse;
import com.training.assetmanagement.access.web.mapper.RoleResponseMapper;
import com.training.assetmanagement.shared.web.ApiAuthority;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

// Đọc thẳng enum - 19 mục cố định, không phân trang
@RestController
@RequestMapping(ApiPath.PERMISSIONS)
@Tag(name = ApiTag.ROLES)
public class PermissionController {

    @GetMapping
    @PreAuthorize(ApiAuthority.ROLE_MANAGE)
    @Operation(operationId = "listPermissions", summary = "Permission catalog with allowed scopes")
    public List<PermissionDefinitionResponse> list() {
        return Arrays.stream(Permission.values()).map(RoleResponseMapper::toDefinition).toList();
    }
}
