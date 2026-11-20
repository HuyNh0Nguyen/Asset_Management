package com.training.assetmanagement.access.repository;

import com.training.assetmanagement.access.domain.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    // LEFT JOIN giữ vai trò chưa có ô quyền - mã của nó vẫn cần cho actor_roles
    @Query(nativeQuery = true, value = """
        SELECT r.code AS roleCode, rp.permission_code AS permissionCode, rp.scope AS scope
        FROM user_roles ur
        JOIN roles r ON r.id = ur.role_id
        LEFT JOIN role_permissions rp ON rp.role_id = ur.role_id
        WHERE ur.user_id = :userId
        ORDER BY r.code
        """)
    List<GrantRow> findGrantRowsByUserId(@Param("userId") UUID userId);

    boolean existsByCode(String code);

    Optional<Role> findByCode(String code);

    List<Role> findByCodeIn(Collection<String> codes);

    // Khóa dòng ADMIN theo uk_roles_code - tuần tự hóa BR-36 (D1)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Role r WHERE r.code = :code")
    Optional<Role> findByCodeForUpdate(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Role r WHERE r.id = :id")
    Optional<Role> findByIdForUpdate(@Param("id") UUID id);
}
