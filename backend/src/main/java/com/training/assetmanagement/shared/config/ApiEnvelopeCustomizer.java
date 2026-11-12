package com.training.assetmanagement.shared.config;

import com.training.assetmanagement.shared.web.ApiErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// Gắn phản hồi lỗi ApiErrorResponse vào MỌI operation; phản hồi 2xx giữ là DTO trần
@Component
public class ApiEnvelopeCustomizer implements GlobalOpenApiCustomizer {

    private static final String ERROR_SCHEMA = "ApiErrorResponse";

    private static final List<HttpStatus> ERROR_STATUSES = List.of(
        HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND,
        HttpStatus.CONFLICT, HttpStatus.UNPROCESSABLE_CONTENT, HttpStatus.INTERNAL_SERVER_ERROR
    );

    @Override
    public void customise(OpenAPI openApi) {
        Components components = Optional.ofNullable(openApi.getComponents()).orElseGet(Components::new);
        openApi.setComponents(components);
        // Sinh schema ApiErrorResponse, ErrorDetail, FieldErrorDetail, ErrorCode (enum)
        ModelConverters.getInstance()
            .readAll(new AnnotatedType(ApiErrorResponse.class))
            .forEach(openApi.getComponents()::addSchemas);

        if (openApi.getPaths() == null) {
            return;
        }
        Content errorContent = new Content().addMediaType(
            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA))
        );

        openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            ApiResponses responses = Optional.ofNullable(operation.getResponses()).orElseGet(ApiResponses::new);
            ERROR_STATUSES.forEach(status -> responses.addApiResponse(String.valueOf(status.value()),
                new ApiResponse().description(status.getReasonPhrase()).content(errorContent)));
            operation.setResponses(responses);
        }));
    }
}
