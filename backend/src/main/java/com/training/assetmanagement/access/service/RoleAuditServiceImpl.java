package com.training.assetmanagement.access.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.audit.domain.*;
import com.training.assetmanagement.audit.service.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Khuôn OrgAuditService: Người thực hiện = CurrentActor; target_code = mã vai trò
@Service
@RequiredArgsConstructor
public class RoleAuditServiceImpl implements RoleAuditService {

    private final CurrentActor currentActor;
    private final AuditLogger auditLogger;

    @Override
    public void log(AuditAction action, UUID roleId, String roleCode, AuditChanges changes) {
        ActorContext actor = currentActor.get();
        auditLogger.log(new AuditActor(actor.userId(), actor.roleCodes()),
            new AuditEvent(action, AuditTargetType.ROLE, roleId, roleCode, changes.oldValue(), changes.newValue()));
    }
}
