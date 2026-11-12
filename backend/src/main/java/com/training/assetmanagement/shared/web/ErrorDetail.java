package com.training.assetmanagement.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Chỉ để chẩn đoán; fieldErrors chỉ có ở 400 do @Valid
public record ErrorDetail(
    @Schema(requiredMode = REQUIRED) OffsetDateTime timestamp,
    @Schema(requiredMode = REQUIRED) int status,
    @Schema(requiredMode = REQUIRED) String error,
    @Schema(requiredMode = REQUIRED) String path,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<FieldErrorDetail> fieldErrors
) {

    public static ErrorDetail of(ErrorCode code, String path, Clock clock) {
        return of(code, path, clock, null);
    }

    public static ErrorDetail of(ErrorCode code, String path, Clock clock, List<FieldErrorDetail> fieldErrors) {
        return new ErrorDetail(OffsetDateTime.now(clock), code.getStatus().value(), code.getStatus().name(), path, fieldErrors);
    }
}
