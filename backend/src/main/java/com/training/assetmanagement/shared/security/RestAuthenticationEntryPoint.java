package com.training.assetmanagement.shared.security;

import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.shared.web.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 401 trong chuỗi filter (chưa đăng nhập, token hết hạn / bị thu hồi) - GlobalExceptionHandler không thấy chỗ này
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorResponseWriter.write(request, response, ErrorCode.UNAUTHORIZED);
    }
}
