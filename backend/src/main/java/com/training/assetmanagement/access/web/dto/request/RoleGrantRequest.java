package com.training.assetmanagement.access.web.dto.request;

import com.training.assetmanagement.access.domain.DataScope;
import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.RoleGrant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "RoleGrantRequest", description = "One cell of the permission matrix")
public record RoleGrantRequest(
    @Schema(requiredMode = REQUIRED)
    @NotNull
    Permission permission,

    @Schema(requiredMode = REQUIRED)
    @NotNull
    DataScope scope
) {
    public RoleGrant toGrant() {
        return new RoleGrant(permission, scope);
    }

    // Mỗi quyền tối đa một ô
    public static boolean uniquePermissions(Collection<RoleGrantRequest> grants) {
        if (grants == null) {
            return true;
        }
        return grants.stream().filter(Objects::nonNull).map(RoleGrantRequest::permission).distinct().count()
            == grants.stream().filter(Objects::nonNull).count();
    }
}
