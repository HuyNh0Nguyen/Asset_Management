package com.training.assetmanagement.notification.web.dto.response;

import com.training.assetmanagement.notification.domain.NotificationEventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "NotificationResponse", description = "In-app notification of the current user")
public record NotificationResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) NotificationEventType eventType,
    @Schema(requiredMode = REQUIRED, description = "Complete Vietnamese sentence, displayed as-is") String content,
    @Schema(description = "Relative in-app path, null when there is nothing to open") String link,
    @Schema(requiredMode = REQUIRED) boolean read,
    @Schema(requiredMode = REQUIRED) OffsetDateTime createdAt
) {
}
