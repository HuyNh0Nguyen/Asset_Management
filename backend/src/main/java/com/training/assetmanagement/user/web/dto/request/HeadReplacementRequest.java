package com.training.assetmanagement.user.web.dto.request;

import com.training.assetmanagement.user.domain.HeadReplacement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// newHeadUserId = null: Để trống trường (tường minh)
@Schema(name = "HeadReplacementRequest", description = "New head for a unit the user currently heads")
public record HeadReplacementRequest(
    @Schema(requiredMode = REQUIRED)
    @NotNull
    UUID unitId,

    UUID newHeadUserId
) {
    public HeadReplacement toReplacement() {
        return new HeadReplacement(unitId, newHeadUserId);
    }
}
