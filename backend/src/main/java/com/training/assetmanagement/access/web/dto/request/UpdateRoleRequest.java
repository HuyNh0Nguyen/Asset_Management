package com.training.assetmanagement.access.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.training.assetmanagement.shared.web.CodeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Không có code: Gửi code -> 400 UNKNOWN_FIELD (fail-on-unknown-properties)
@Schema(name = "UpdateRoleRequest", description = "Update role name, description and permission matrix")
public record UpdateRoleRequest(
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
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue
    public boolean isGrantsUnique() {
        return RoleGrantRequest.uniquePermissions(grants);
    }
}
