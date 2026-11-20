package com.training.assetmanagement.access.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.training.assetmanagement.shared.web.CodeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "CreateRoleRequest", description = "Create a role with its permission matrix")
public record CreateRoleRequest(
    @Schema(requiredMode = REQUIRED)
    @NotBlank
    @Size(max = CodeFormat.CODE_MAX_LENGTH)
    @Pattern(regexp = CodeFormat.PATTERN)
    String code,

    @Schema(requiredMode = REQUIRED)
    @NotBlank
    @Size(max = CodeFormat.NAME_MAX_LENGTH)
    String name,

    @Size(max = CodeFormat.DESCRIPTION_MAX_LENGTH)
    String description,

    @Schema(requiredMode = REQUIRED)
    @NotNull
    List<@NotNull @Valid RoleGrantRequest> grants
) {
    // fieldErrors.field = "grantsUnique"
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue
    public boolean isGrantsUnique() {
        return RoleGrantRequest.uniquePermissions(grants);
    }
}
