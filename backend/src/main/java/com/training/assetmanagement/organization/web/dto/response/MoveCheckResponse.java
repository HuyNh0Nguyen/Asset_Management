package com.training.assetmanagement.organization.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// blockingHeads rỗng = chuyển được
@Schema(name = "MoveCheckResponse", description = "Response indicating whether an organizational unit can be moved, including any blocking heads")
public record MoveCheckResponse(List<BlockingHeadResponse> blockingHeads) {
}
