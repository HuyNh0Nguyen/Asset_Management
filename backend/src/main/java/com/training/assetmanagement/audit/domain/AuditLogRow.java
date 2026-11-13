package com.training.assetmanagement.audit.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

// Một dòng audit_logs, cột JSON đã chuyển sang chuỗi
@Schema(name = "AuditLogRow", description = "A single row from the audit_logs table with JSON columns as strings")
public record AuditLogRow(
    UUID id,
    UUID actorId,
    String actorRolesJson,
    AuditAction action,
    AuditTargetType targetType,
    UUID targetId,
    String targetCode,
    String oldValueJson,
    String newValueJson,
    LocalDateTime createdAt
) {
}
