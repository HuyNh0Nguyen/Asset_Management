package com.training.assetmanagement.user.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(name = "DeactivateUserRequest", description = "Deactivate a user and replace the heads of units they lead")
public record DeactivateUserRequest(
    List<@NotNull @Valid HeadReplacementRequest> headReplacements
) {
    public DeactivateUserRequest {
        headReplacements = headReplacements == null ? List.of() : headReplacements;
    }
}
