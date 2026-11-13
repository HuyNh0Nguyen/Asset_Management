package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.Rank;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import com.training.assetmanagement.organization.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationLookupServiceImpl implements OrganizationLookupService {

    private final OrgUnitRepository orgUnitRepository;
    private final RankRepository rankRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<OrgUnit> findOrgUnit(UUID orgUnitId) {
        return orgUnitRepository.findById(orgUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rank> findRank(UUID rankId) {
        return rankRepository.findById(rankId);
    }
}
