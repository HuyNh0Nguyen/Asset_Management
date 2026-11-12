package com.training.assetmanagement.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

// Mọi thuộc tính app.* - thiếu một giá trị bắt buộc thì ứng dụng dừng ngay khi khởi động và nêu tên
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Db db,
        @Valid @NotNull Storage storage,
        @Valid @NotNull Jwt jwt,
        @NotBlank String publicBaseUrl,
        boolean seedDemo
) {
    public record Db(@NotBlank String host) {}

    public record Storage(
        @NotBlank String bucket,
        @NotNull Duration presignedTtl,
        @NotBlank String signingRegion,
        @NotBlank String internalEndpoint,      // Backend ghi qua đây (mạng nội bộ)
        @NotBlank String publicEndpoint,        // Origin của trình duyệt - dùng để ký URL
        @NotBlank String accessKey,
        @NotBlank String secretKey
    ) {}

    public record Jwt(@NotBlank @Size(min = 32) String secret) {}
}
