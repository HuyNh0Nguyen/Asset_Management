package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditActor;
import com.training.assetmanagement.audit.domain.AuditEvent;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.audit.service.AuditLogger;
import com.training.assetmanagement.organization.domain.OrgAuditChanges;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgAuditServiceImpl implements OrgAuditService {

    private final CurrentActor currentActor;
    private final AuditLogger auditLogger;

    @Override
    public void log(AuditAction action, AuditTargetType targetType, UUID targetId, String targetCode,
                    OrgAuditChanges changes) {
        AuditEvent event = new AuditEvent(action, targetType, targetId, targetCode,
            changes.oldValue(), changes.newValue());
        auditLogger.log(toAuditActor(currentActor.get()), event);
    }

    // Cùng khuôn với PasswordServiceImpl.toAuditActor
    private static AuditActor toAuditActor(ActorContext actor) {
        return new AuditActor(actor.userId(), actor.roleCodes());
    }
}
