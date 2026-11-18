package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "RankResponse", description = "Response object for rank-related operations")
public record RankResponse(
    @Schema(requiredMode = REQUIRED) UUID id,
    @Schema(requiredMode = REQUIRED) String code,
    @Schema(requiredMode = REQUIRED) String name,
    @Schema(requiredMode = REQUIRED) int seniority
) {
}
