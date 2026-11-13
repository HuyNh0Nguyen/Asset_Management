package com.training.assetmanagement.audit.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

// oldValue / newValue chỉ gồm trường thay đổi; không bao giờ chứa mật khẩu hay hash
@Schema(name = "AuditEvent", description = "An event representing an action performed by an actor, including details of the changes made")
public record AuditEvent(
    AuditAction action,
    AuditTargetType targetType,
    UUID targetId,
    String targetCode,
    Map<String, Object> oldValue,
    Map<String, Object> newValue
) {
    // Thao tác không đổi dữ liệu (đăng nhập, đăng xuất...) để trống cả hai cột
    public static AuditEvent withoutChange(AuditAction action, AuditTargetType targetType,
                                           UUID targetId, String targetCode) {
        return new AuditEvent(action, targetType, targetId, targetCode, null, null);
    }
}
