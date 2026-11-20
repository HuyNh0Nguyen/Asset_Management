package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.*;
import com.training.assetmanagement.audit.service.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

// target_code = username; actor_roles = vai trò lúc bắt đầu request
@Service
@RequiredArgsConstructor
public class UserAuditServiceImpl implements UserAuditService {

    private final CurrentActor currentActor;
    private final AuditLogger auditLogger;

    @Override
    public void log(AuditAction action, UUID userId, String username, AuditChanges changes) {
        ActorContext actor = currentActor.get();
        auditLogger.log(new AuditActor(actor.userId(), actor.roleCodes()),
            new AuditEvent(action, AuditTargetType.USER, userId, username, changes.oldValue(), changes.newValue()));
    }
}
