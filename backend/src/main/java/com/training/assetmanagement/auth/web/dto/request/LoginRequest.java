package com.training.assetmanagement.auth.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Request for user login")
public record LoginRequest(
        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
