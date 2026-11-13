package com.training.assetmanagement.organization.repository;

import com.training.assetmanagement.organization.domain.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, UUID> {
}
