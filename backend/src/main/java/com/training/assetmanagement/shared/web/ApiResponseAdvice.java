package com.training.assetmanagement.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// Bọc phong bì thành công - controller KHÔNG tự gọi ApiResponse.success()
@RestControllerAdvice(basePackages = ApiResponseAdvice.BASE_PACKAGE)
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    // Controller nằm rải ở 14 package <miền>.web; springdoc, actuator ở ngoài nên tự không bị bọc
    static final String BASE_PACKAGE = "com.training.assetmanagement";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Chỉ bọc khi ghi JSON; Resource / byte[] / StreamingResponseBody đi converter khác
        return AbstractJacksonHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // Thân đã là phong bì (từ GlobalExceptionHandler) thì để nguyên
        return body instanceof ApiResponse<?> ? body : ApiResponse.success(body);
    }
}
