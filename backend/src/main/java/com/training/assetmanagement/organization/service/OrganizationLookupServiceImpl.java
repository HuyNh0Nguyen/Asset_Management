package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.*;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import com.training.assetmanagement.organization.repository.RankRepository;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationLookupServiceImpl implements OrganizationLookupService {

    private final OrgUnitRepository orgUnitRepository;
    private final RankRepository rankRepository;
    private final OrgTreeService orgTreeService;
    private final OrgUnitService orgUnitService;

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

    @Override
    @Transactional(readOnly = true)
    public OrgTree loadTree() {
        return orgTreeService.load();
    }

    // MANDATORY: Khóa phải nằm trong giao dịch của bên gọi, như AuditLogger
    // Khóa đọc va với khóa ghi của ngừng hoạt động
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public OrgUnit requireActiveUnit(UUID unitId, Set<OrgUnitType> allowedTypes) {
        OrgUnit unit = orgUnitRepository.findByIdForShare(unitId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (unit.getStatus() != OrgUnitStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ORG_UNIT_INACTIVE);
        }
        if (!allowedTypes.contains(unit.getUnitType())) {
            throw new BusinessException(ErrorCode.ORG_UNIT_TYPE_NOT_ALLOWED);
        }
        return unit;
    }

    @Override
    @Transactional(readOnly = true)
    public Rank requireRank(UUID rankId) {
        return rankRepository.findById(rankId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> unitsHeadedBy(UUID userId) {
        return orgUnitRepository.findIdsByHeadUserId(userId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceHead(UUID unitId, UUID newHeadUserId) {
        orgUnitService.replaceHead(unitId, newHeadUserId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<OrgUnit> lockUnitsHeadedBy(UUID userId) {
        return orgUnitRepository.findByHeadUserIdForUpdate(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rank> listRanks() {
        return rankRepository.findAll();
    }
}
