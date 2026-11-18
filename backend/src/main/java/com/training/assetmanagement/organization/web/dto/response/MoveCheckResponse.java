package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// blockingHeads rỗng = chuyển được
@Schema(name = "MoveCheckResponse", description = "Response indicating whether an organizational unit can be moved, including any blocking heads")
public record MoveCheckResponse(
    @Schema(requiredMode = REQUIRED) List<BlockingHeadResponse> blockingHeads
) {
}
