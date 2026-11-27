package com.training.assetmanagement.emailservice.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        @NotBlank
        @Email
        String fromAddress,

        @NotBlank
        String fromName,

        // Số lần thử lại SMTP - tổng tối đa maxRetries + 1 lần gửi
        @Min(0)
        int maxRetries,

        @NotNull
        Duration retryDelay,

        // Trần số lần hoãn khi CSDL sập (12 x 5 phút ≈ 1 giờ)
        @Min(0)
        int maxDeferrals
) {
}
