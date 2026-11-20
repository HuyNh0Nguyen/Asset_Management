package com.training.assetmanagement.user.web.dto.request;

import com.training.assetmanagement.user.domain.UserFieldFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// roleIds ≥ 1; không tự thêm EMPLOYEE - "tích sẵn" là việc của form
@Schema(name = "CreateUserRequest", description = "Create a user account")
public record CreateUserRequest(
    @Schema(requiredMode = REQUIRED)
    @NotBlank
    @Pattern(regexp = UserFieldFormat.USERNAME_PATTERN)
    String username,

    @Schema(requiredMode = REQUIRED)
    @NotBlank
    @Email
    @Size(max = UserFieldFormat.EMAIL_MAX_LENGTH)
    String email,

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
    Set<@NotNull UUID> roleIds
) {
}
