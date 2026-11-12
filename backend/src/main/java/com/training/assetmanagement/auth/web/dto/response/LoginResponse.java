package com.training.assetmanagement.auth.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

// Thông tin người dùng lấy tiếp qua GET /api/me
@Schema(name = "LoginResponse", description = "Response for login operations")
public record LoginResponse(
        @Schema(requiredMode = REQUIRED) String accessToken,
        @Schema(requiredMode = REQUIRED) boolean mustChangePassword
) {
}
