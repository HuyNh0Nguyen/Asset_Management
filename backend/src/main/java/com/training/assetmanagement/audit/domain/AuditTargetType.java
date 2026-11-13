package com.training.assetmanagement.audit.domain;

// Khớp CHECK ck_audit_logs_target_type
public enum AuditTargetType {
    ORG_UNIT, RANK, USER, ROLE, ASSET_TYPE, SUPPLIER, ASSET, ASSET_BATCH,
    ALLOCATION, MAINTENANCE_TICKET, REQUEST, WORKFLOW, APPROVAL_TASK, SYSTEM_CONFIG
}
