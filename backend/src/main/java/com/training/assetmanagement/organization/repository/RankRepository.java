package com.training.assetmanagement.organization.repository;

import com.training.assetmanagement.organization.domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RankRepository extends JpaRepository<Rank, UUID> {
}
