package com.training.assetmanagement.access.repository;

import com.training.assetmanagement.access.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByUserIdIn(Collection<UUID> userIds);

    // Đếm mọi trạng thái người dùng - RESTRICT cũng tính cả người đã nghỉ
    long countByRoleId(UUID roleId);

    @Query("SELECT ur.userId FROM UserRole ur WHERE ur.roleId = :roleId")
    List<UUID> findUserIdsByRoleId(@Param("roleId") UUID roleId);

    @Query("""
        SELECT NEW com.training.assetmanagement.access.repository.RoleHolderCount(ur.roleId, COUNT(ur.id))
        FROM UserRole ur GROUP BY ur.roleId
        """)
    List<RoleHolderCount> countHoldersByRole();

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId IN :roleIds")
    int deleteByUserIdAndRoleIds(@Param("userId") UUID userId, @Param("roleIds") Collection<UUID> roleIds);
}
