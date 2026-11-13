package com.training.assetmanagement.shared.web;

import io.swagger.v3.oas.annotations.media.Schema;

// Phong bì cho mọi thân JSON: { code, message, data }
@Schema(name = "ApiResponse", description = "Envelope for all JSON responses")
public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SuccessCode.SUCCESS.name(), SuccessCode.SUCCESS.getMessage(), data);
    }

    public static ApiResponse<ErrorDetail> error(ErrorCode errorCode, ErrorDetail detail) {
        return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), detail);
    }
}
