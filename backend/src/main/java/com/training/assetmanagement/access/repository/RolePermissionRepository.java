package com.training.assetmanagement.access.repository;

import com.training.assetmanagement.access.domain.Permission;
import com.training.assetmanagement.access.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByRoleIdIn(Collection<UUID> roleIds);

    // Người giữ quyền P qua mọi vai trò (mọi trạng thái) - bước 1 của chiều ngược
    @Query("""
        SELECT NEW com.training.assetmanagement.access.repository.HolderGrant(ur.userId, rp.scope)
        FROM UserRole ur JOIN RolePermission rp ON rp.roleId = ur.roleId
        WHERE rp.permission = :permission
        """)
    List<HolderGrant> findHolderGrants(@Param("permission") Permission permission);
}
