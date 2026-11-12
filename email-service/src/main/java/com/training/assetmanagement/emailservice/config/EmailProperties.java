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

        @Min(0)
        int maxRetries,

        @NotNull
        Duration retryDelay
) {
}
