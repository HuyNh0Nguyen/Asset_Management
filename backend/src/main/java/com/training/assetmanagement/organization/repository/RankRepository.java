package com.training.assetmanagement.organization.repository;

import com.training.assetmanagement.organization.domain.Rank;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RankRepository extends JpaRepository<Rank, UUID> {

    List<Rank> findAllByOrderBySeniorityAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rank r WHERE r.id = :id")
    Optional<Rank> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByCode(String code);

    boolean existsBySeniority(int seniority);

    boolean existsBySeniorityAndIdNot(int seniority, UUID id);
}
