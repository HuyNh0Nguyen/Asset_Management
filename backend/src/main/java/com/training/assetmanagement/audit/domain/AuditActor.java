package com.training.assetmanagement.audit.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

// Người thực hiện luôn là tài khoản thật; roleCodes chụp lúc thao tác
@Schema(name = "AuditActor", description = "Information about the actor performing an action for auditing purposes")
public record AuditActor(UUID actorId, List<String> roleCodes) {
}
