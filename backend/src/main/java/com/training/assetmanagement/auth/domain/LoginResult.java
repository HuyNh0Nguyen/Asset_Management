package com.training.assetmanagement.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;

// Kết quả đăng nhập thành công ở tầng service - controller đổi sang LoginResponse
@Schema(name = "LoginResult", description = "Result of a successful login operation")
public record LoginResult(String accessToken, boolean mustChangePassword) {
}
