package com.training.assetmanagement.shared.security;

import com.training.assetmanagement.shared.web.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

// Mang ErrorCode từ filter tới entry point qua thuộc tính request - filter không tự ghi response
public final class AuthFailure {

    private static final String ATTRIBUTE = AuthFailure.class.getName();

    public static void mark(HttpServletRequest request, ErrorCode errorCode) {
        request.setAttribute(ATTRIBUTE, errorCode);
    }

    public static Optional<ErrorCode> of(HttpServletRequest request) {
        return request.getAttribute(ATTRIBUTE) instanceof ErrorCode errorCode
            ? Optional.of(errorCode)
            : Optional.empty();
    }

    private AuthFailure() {}
}
