package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.Rank;

import java.util.Optional;
import java.util.UUID;

// Miền khác đọc org_units / ranks qua đây
public interface OrganizationLookupService {

    Optional<OrgUnit> findOrgUnit(UUID orgUnitId);

    Optional<Rank> findRank(UUID rankId);
}
