package com.training.assetmanagement.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "ApiErrorResponse", description = "Envelope for error response")
public record ApiErrorResponse(
        @Schema(requiredMode = REQUIRED) ErrorCode code,
        @Schema(requiredMode = REQUIRED) String message,
        @Schema(requiredMode = REQUIRED) ErrorDetail data
) {
}
