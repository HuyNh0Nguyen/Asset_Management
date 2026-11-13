package com.training.assetmanagement.shared.security;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

// Token đã qua chữ ký + hạn: Chỉ còn id người dùng và thời điểm phát hành
@Schema(name = "VerifiedToken", description = "Verified JWT token with user ID and issued time")
public record VerifiedToken(
    UUID userId,
    Instant issuedAt
) {
}
