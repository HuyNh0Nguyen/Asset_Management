package com.training.assetmanagement.user.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "AvatarResponse", description = "Signed thumbmail URL of the new avatar")
public record AvatarResponse(
    @Schema(requiredMode = REQUIRED) String avatarUrl
) {
}
