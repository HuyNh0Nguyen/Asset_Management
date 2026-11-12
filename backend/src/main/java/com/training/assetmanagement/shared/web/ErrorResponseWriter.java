package com.training.assetmanagement.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

@Component
public class ErrorResponseWriter {

    private final JsonMapper jsonMapper;
    private final Clock clock;

    public ErrorResponseWriter(JsonMapper jsonMapper, Clock clock) {
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(),
            ApiResponse.error(errorCode, ErrorDetail.of(errorCode, request.getRequestURI(), clock)));
    }
}
