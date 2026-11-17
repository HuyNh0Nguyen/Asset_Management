package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RankResponse", description = "Response object for rank-related operations")
public record RankResponse(UUID id, String code, String name, int seniority) {
}
