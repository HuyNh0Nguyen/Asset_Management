package com.training.assetmanagement.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "FieldErrorDetail", description = "Detail of a field validation error")
public record FieldErrorDetail(
        @Schema(requiredMode = REQUIRED) String field,
        @Schema(requiredMode = REQUIRED) String message
) {
}
