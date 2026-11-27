package com.training.assetmanagement.notification.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "UnreadCountResponse", description = "Number of unread notifications of the current user")
public record UnreadCountResponse(
    @Schema(requiredMode = REQUIRED) long unreadCount
) {
}
