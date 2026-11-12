package com.training.assetmanagement.shared.web;

// Phong bì cho mọi thân JSON: { code, message, data }
public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SuccessCode.SUCCESS.name(), SuccessCode.SUCCESS.getMessage(), data);
    }

    public static ApiResponse<ErrorDetail> error(ErrorCode errorCode, ErrorDetail detail) {
        return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), detail);
    }
}
