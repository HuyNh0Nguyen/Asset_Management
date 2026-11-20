package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

// newHeadUserId = null nghĩa là để trống (tường minh)
@Schema(name = "HeadReplacement", description = "Represents a head replacement action, which may involve replacing the current head user with a new one. If the new head user ID is null, it indicates that the head position is being left vacant")
public record HeadReplacement(UUID unitId, UUID newHeadUserId) {
}
