package com.training.assetmanagement.access.service;

import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;

import java.util.UUID;

public interface RoleAuditService {

    void log(AuditAction action, UUID roleId, String roleCode, AuditChanges changes);
}
