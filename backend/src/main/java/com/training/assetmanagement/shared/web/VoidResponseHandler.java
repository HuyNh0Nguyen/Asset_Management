package com.training.assetmanagement.shared.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

// Method void trong @RestController của dự án -> 200 kèm "data": null (không dùng 204)
@RequiredArgsConstructor
public class VoidResponseHandler implements HandlerMethodReturnValueHandler {

    private final JsonMapper jsonMapper;

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> controller = returnType.getContainingClass();
        return void.class.equals(returnType.getParameterType())
            && controller.getPackageName().startsWith(ApiResponseAdvice.BASE_PACKAGE)
            && AnnotatedElementUtils.hasAnnotation(controller, RestController.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        HttpServletResponse response = Objects.requireNonNull(webRequest.getNativeResponse(HttpServletResponse.class));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), ApiResponse.success(null));
    }
}
