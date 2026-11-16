package com.training.assetmanagement.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Chỉ để chẩn đoán; fieldErrors chỉ có ở 400 do @Valid
// retryAfterSeconds chỉ có ở lỗi kèm thời gian chờ (ACCOUNT_LOCKED) - FE dùng để đếm ngược
@Schema(name = "ErrorDetail", description = "Error response with timestamp, status, error message, request path, and optional field errors")
public record ErrorDetail(
    @Schema(requiredMode = REQUIRED) OffsetDateTime timestamp,
    @Schema(requiredMode = REQUIRED) int status,
    @Schema(requiredMode = REQUIRED) String error,
    @Schema(requiredMode = REQUIRED) String path,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<FieldErrorDetail> fieldErrors,
    @JsonInclude(JsonInclude.Include.NON_NULL) Long retryAfterSeconds
) {

    public static ErrorDetail of(ErrorCode code, String path, Clock clock) {
        return of(code, path, clock, null, null);
    }

    public static ErrorDetail of(ErrorCode code, String path, Clock clock, List<FieldErrorDetail> fieldErrors) {
        return of(code, path, clock, fieldErrors, null);
    }

    public static ErrorDetail of(ErrorCode code, String path, Clock clock, List<FieldErrorDetail> fieldErrors,
                                 Long retryAfterSeconds) {
        return new ErrorDetail(OffsetDateTime.now(clock), code.getStatus().value(), code.getStatus().name(), path,
            fieldErrors, retryAfterSeconds);
    }
}
