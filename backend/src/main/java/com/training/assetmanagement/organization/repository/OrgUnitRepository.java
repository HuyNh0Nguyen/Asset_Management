package com.training.assetmanagement.organization.repository;

import com.training.assetmanagement.organization.domain.OrgNode;
import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.OrgUnitStatus;
import com.training.assetmanagement.organization.domain.OrgUnitType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {

    // Cả cây trong một truy vấn - nguồn của OrgTree
    @Query("""
        SELECT new com.training.assetmanagement.organization.domain.OrgNode(
            u.id, u.code, u.name, u.unitType, u.parentId, u.headUserId, u.status)
        FROM OrgUnit u
        """)
    List<OrgNode> findAllNodes();

    // SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM OrgUnit u WHERE u.id = :id")
    Optional<OrgUnit> findByIdForUpdate(@Param("id") UUID id);

    // SELECT ... LOCK IN SHARE MODE trên MariaDB - kiểm trong log show-sql
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT u FROM OrgUnit u WHERE u.id = :id")
    Optional<OrgUnit> findByIdForShare(@Param("id") UUID id);

    @Query("SELECT u.id FROM OrgUnit u WHERE u.unitType = :unitType")
    List<UUID> findIdsByUnitType(@Param("unitType") OrgUnitType unitType);

    @Query("SELECT u.id FROM OrgUnit u WHERE u.headUserId = :userId")
    List<UUID> findIdsByHeadUserId(@Param("userId") UUID userId);

    boolean existsByCode(String code);

    // CAS trạng thái: 0 dòng = trạng thái đã bị đổi bởi thao tác khác
    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE OrgUnit u
        SET u.status = :nextStatus, u.updatedBy = :actorId, u.updatedAt = :changedAt
        WHERE u.id = :id AND u.status = :currentStatus
        """)
    int updateStatus(@Param("id") UUID id,
                     @Param("currentStatus") OrgUnitStatus currentStatus,
                     @Param("nextStatus") OrgUnitStatus nextStatus,
                     @Param("actorId") UUID actorId,
                     @Param("changedAt") LocalDateTime changedAt);
}
