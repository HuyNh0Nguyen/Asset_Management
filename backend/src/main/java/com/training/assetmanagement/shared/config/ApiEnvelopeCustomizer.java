package com.training.assetmanagement.shared.config;

import com.training.assetmanagement.shared.web.ApiErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Chỉ đăng ký schema lỗi vào components để FE sinh model (ApiErrorResponse, ErrorCode...).
// Không gắn phản hồi lỗi vào từng operation: Swagger UI chỉ hiện mẫu phản hồi thành công (2xx)
@Component
public class ApiEnvelopeCustomizer implements GlobalOpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        Components components = Optional.ofNullable(openApi.getComponents()).orElseGet(Components::new);
        openApi.setComponents(components);
        // Sinh schema ApiErrorResponse, ErrorDetail, FieldErrorDetail, ErrorCode (enum)
        ModelConverters.getInstance()
            .readAll(new AnnotatedType(ApiErrorResponse.class))
            .forEach(components::addSchemas);
    }
}
