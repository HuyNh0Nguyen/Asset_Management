package com.training.assetmanagement.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RankRef", description = "Represents a reference to a rank, which may be used to identify or link to a specific rank within the system")
public record RankRef(UUID id, String code, String name) {
}
