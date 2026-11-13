package com.training.assetmanagement.user.repository;

import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAuthSnapshot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<UUID> findIdByUsername(@Param("username") String username);

    @Query("""
        SELECT NEW com.training.assetmanagement.user.domain.UserAuthSnapshot(
            u.id, u.username, u.status, u.tokenValidFrom, u.mustChangePassword, u.orgUnitId)
        FROM User u
        WHERE u.id = :id
        """)
    Optional<UserAuthSnapshot> findAuthSnapshotById(@Param("id") UUID id);

    // SELECT ... FOR UPDATE: hai lần sai đồng thời không làm mất một lần đếm
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    // Bộ đếm không phải sửa nghiệp vụ - không chạm updated_by / updated_at
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u SET u.failedLoginCount = :count, u.lockedUntil = :lockedUntil WHERE u.id = :id")
    int updateLoginFailures(@Param("id") UUID id,
                            @Param("count") int count,
                            @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u SET u.tokenValidFrom = :validFrom WHERE u.id = :id")
    int updateTokenValidFrom(@Param("id") UUID id, @Param("validFrom") LocalDateTime validFrom);

    // Một lệnh ghi cho đổi / đặt lại mật khẩu: hash, cờ, thu hồi token, gỡ khóa
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE User u
        SET u.passwordHash = :passwordHash,
            u.mustChangePassword = :mustChangePassword,
            u.tokenValidFrom = :validFrom,
            u.failedLoginCount = 0,
            u.lockedUntil = null,
            u.updatedBy = :updatedBy,
            u.updatedAt = :updatedAt
        WHERE u.id = :id
        """)
    int updatePassword(@Param("id") UUID id,
                       @Param("passwordHash") String passwordHash,
                       @Param("mustChangePassword") boolean mustChangePassword,
                       @Param("validFrom") LocalDateTime validFrom,
                       @Param("updatedBy") UUID updatedBy,
                       @Param("updatedAt") LocalDateTime updatedAt);
}
