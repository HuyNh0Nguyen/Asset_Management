package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrgTreeServiceImpl implements OrgTreeService {

    private final OrgUnitRepository orgUnitRepository;

    // Dữ liệu vòng / mồ côi -> IllegalStateException -> GlobalExceptionHandler log ERROR, trả 500
    @Override
    @Transactional(readOnly = true)
    public OrgTree load() {
        return OrgTree.of(orgUnitRepository.findAllNodes());
    }
}
