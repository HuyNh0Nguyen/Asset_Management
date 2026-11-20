package com.training.assetmanagement.user.web.dto.response;

import com.training.assetmanagement.user.domain.UserBlockerKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "UserBlockerResponse", description = "A group of items blocking user deactivation")
public record UserBlockerResponse(
    @Schema(requiredMode = REQUIRED) UserBlockerKind kind,
    @Schema(requiredMode = REQUIRED) long total,
    @Schema(requiredMode = REQUIRED) List<UserBlockerItemResponse> samples
) {
}
