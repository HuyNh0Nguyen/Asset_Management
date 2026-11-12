package com.training.assetmanagement.shared.config;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    static {
        // Enum xuất thành schema có tên (ErrorCode, Permission...) - FE nhận enum, không phải string
        ModelResolver.enumsAsRef = true;
    }

    @Bean
    public OpenAPI assetOpenApi() {
        return new OpenAPI()
            .info(new Info().title("Asset Management API").version("v1"))
            // Server cố định: bản chụp hợp đồng giống nhau ở mọi môi trường
            .servers(List.of(new Server().url("/").description("Same origin")))
            // Nút Authorize của Swagger
            .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
