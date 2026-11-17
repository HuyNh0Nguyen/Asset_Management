package com.training.assetmanagement.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
        boolean seedDemo,
        @Valid Demo demo
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

    // HS256 cần secret >= 32 byte
    public record Jwt(
        @NotBlank @Size(min = 32) String secret,
        @NotNull Duration ttl
    ) {}

    // Hộp thư Gmail nhận thư của người dùng demo qua bí danh
    public record Demo(@Email String mailbox) {}

    // Bật seed demo thì bắt buộc có hộp thư - ứng dụng dừng khi khởi động
    @AssertTrue(message = "app.demo.mailbox is required when app.seed-demo=true")
    public boolean isDemoMailboxConfigured() {
        return !seedDemo || (demo != null && demo.mailbox() != null && !demo.mailbox().isBlank());
    }
}
