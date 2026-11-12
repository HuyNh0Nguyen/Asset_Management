package com.training.assetmanagement.shared.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Đặt VoidResponseHandler lên ĐẦU danh sách - thêm qua WebMvcConfigurer thì đứng sau handler mặc định, không bao giờ được gọi
@Component
@RequiredArgsConstructor
public class VoidResponseRegistrar implements InitializingBean {

    private final RequestMappingHandlerAdapter handlerAdapter;
    private final JsonMapper jsonMapper;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();
        handlers.add(new VoidResponseHandler(jsonMapper));
        handlers.addAll(Objects.requireNonNull(handlerAdapter.getReturnValueHandlers()));
        handlerAdapter.setReturnValueHandlers(handlers);
    }
}
