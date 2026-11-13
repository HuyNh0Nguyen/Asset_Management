package com.training.assetmanagement.shared.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger UI gửi Bearer token cho mọi operation sau khi bấm Authorize
@Configuration
@SecurityScheme(name = OpenApiSecurityConfig.BEARER_SCHEME, type = SecuritySchemeType.HTTP,
            scheme = "bearer", bearerFormat = "JWT")
public class OpenApiSecurityConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public GlobalOpenApiCustomizer bearerSecurityCustomizer() {
        return openApi -> openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
