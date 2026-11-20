package com.training.assetmanagement.access.domain;

// Độc lập với AuditTargetType - AuditMasker tự ánh xạ, tránh vòng access <-> audit
public enum FinanceTarget {
    ASSET_TYPE,
    ASSET,
    ASSET_BATCH,
    MAINTENANCE_TICKET,
    REQUEST,
    DISPOSAL_LINE
}
