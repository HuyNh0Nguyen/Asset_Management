package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.audit.domain.AuditTargetType;

import java.util.UUID;

public interface OrgAuditService {

    // Gọi Audit trong giao dịch nghiệp vụ đang mở (AuditLogger là MANDATORY), người thực hiện = actor hiện tại
    void log(AuditAction action, AuditTargetType targetType, UUID targetId, String targetCode, AuditChanges changes);
}
