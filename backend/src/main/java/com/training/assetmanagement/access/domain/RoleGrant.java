package com.training.assetmanagement.access.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

// Một ô ma trận: quyền -> phạm vi
@Schema(name = "RoleGrant", description = "A grant of a permission with a data scope")
public record RoleGrant(Permission permission, DataScope scope) {

    // Sắp theo thứ tự enum - so ma trận cũ / mới và ghi Audit ổn định
    public static List<RoleGrant> sorted(Collection<RoleGrant> grants) {
        return grants.stream().sorted(Comparator.comparing(RoleGrant::permission)).toList();
    }
}
