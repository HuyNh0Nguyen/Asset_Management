package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnitType;
import com.training.assetmanagement.organization.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class OrgRootLocatorImpl implements OrgRootLocator {

    private final OrgUnitRepository orgUnitRepository;
    private final TransactionTemplate separateReadOnly;
    private volatile UUID rootId;

    public OrgRootLocatorImpl(OrgUnitRepository orgUnitRepository, PlatformTransactionManager transactionManager) {
        this.orgUnitRepository = orgUnitRepository;
        // REQUIRES_NEW: Tạm dừng giao dịch của bên gọi, đọc trên kết nối riêng
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(true);
        this.separateReadOnly = template;
    }

    @Override
    public UUID rootId() {
        UUID cached = rootId;
        if (cached == null) {
            cached = separateReadOnly.execute(status -> loadRootId());
            rootId = cached;
        }
        return cached;
    }

    private UUID loadRootId() {
        List<UUID> ids = orgUnitRepository.findIdsByUnitType(OrgUnitType.COMPANY);
        if (ids.size() != 1) {
            throw new IllegalStateException("Expected exactly one COMPANY org unit, found " + ids.size());
        }
        return ids.get(0);
    }
}
