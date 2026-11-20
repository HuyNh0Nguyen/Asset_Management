package com.training.assetmanagement.user.web.dto.request;

import com.training.assetmanagement.user.domain.UserFieldFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Không có username / email: Gửi lên -> 400 UNKNOWN_FIELD
@Schema(name = "UpdateUserRequest", description = "Update a user account")
public record UpdateUserRequest(
    @Schema(requiredMode = REQUIRED)
    @NotBlank
    @Size(max = UserFieldFormat.FULL_NAME_MAX_LENGTH)
    String fullName,

    @Pattern(regexp = UserFieldFormat.PHONE_PATTERN)
    String phoneNumber,

    @Schema(requiredMode = REQUIRED)
    @NotNull
    UUID orgUnitId,

    @Schema(requiredMode = REQUIRED)
    @NotNull
    UUID rankId,

    @Schema(requiredMode = REQUIRED)
    @NotEmpty
    Set<@NotNull UUID> roleIds,

    List<@NotNull @Valid HeadReplacementRequest> headReplacements
) {
    public UpdateUserRequest {
        headReplacements = headReplacements == null ? List.of() : headReplacements;
    }
}
