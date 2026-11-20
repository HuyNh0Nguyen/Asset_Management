package com.training.assetmanagement.access.web;

import com.training.assetmanagement.access.service.RoleService;
import com.training.assetmanagement.access.web.dto.request.CreateRoleRequest;
import com.training.assetmanagement.access.web.dto.request.RoleGrantRequest;
import com.training.assetmanagement.access.web.dto.request.UpdateRoleRequest;
import com.training.assetmanagement.access.web.dto.response.RoleResponse;
import com.training.assetmanagement.access.web.mapper.RoleResponseMapper;
import com.training.assetmanagement.shared.web.ApiAuthority;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Danh sách vai trò là dữ liệu tham chiếu (vài dòng, form người dùng cần đủ) - trả trọn như cây đơn vị
@RestController
@RequestMapping(ApiPath.ROLES)
@Tag(name = ApiTag.ROLES)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize(ApiAuthority.ROLE_OR_USER_MANAGE)
    @Operation(operationId = "listRoles", summary = "List roles with their permission matrix")
    public List<RoleResponse> list() {
        return roleService.list().stream().map(RoleResponseMapper::toResponse).toList();
    }

    @GetMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ROLE_MANAGE)
    @Operation(operationId = "getRole", summary = "Get a role with its permission matrix")
    public RoleResponse get(@PathVariable UUID id) {
        return RoleResponseMapper.toResponse(roleService.get(id));
    }

    @PostMapping
    @PreAuthorize(ApiAuthority.ROLE_MANAGE)
    @Operation(operationId = "createRole", summary = "Create a role")
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return RoleResponseMapper.toResponse(roleService.create(request.code(), request.name(),
            request.description(), request.grants().stream().map(RoleGrantRequest::toGrant).toList()));
    }

    @PutMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ROLE_MANAGE)
    @Operation(operationId = "updateRole", summary = "Update role name, description and permission matrix")
    public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return RoleResponseMapper.toResponse(roleService.update(id, request.name(), request.description(),
            request.grants().stream().map(RoleGrantRequest::toGrant).toList()));
    }

    @DeleteMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ROLE_MANAGE)
    @Operation(operationId = "deleteRole", summary = "Delete an unused role")
    public void delete(@PathVariable UUID id) {
        roleService.delete(id);
    }
}
