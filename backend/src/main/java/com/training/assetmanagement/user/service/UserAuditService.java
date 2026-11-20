package com.training.assetmanagement.user.service;

import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;

import java.util.UUID;

public interface UserAuditService {

    void log(AuditAction action, UUID userId, String username, AuditChanges changes);
}
