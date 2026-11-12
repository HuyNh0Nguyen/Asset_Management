package com.training.assetmanagement.shared.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// message tiếng Anh chỉ để log; FE hiển thị câu tiếng Việt theo tên mã
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 - dữ liệu đầu vào
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Malformed request"),
    UNKNOWN_FIELD(HttpStatus.BAD_REQUEST, "Request contains an unknown field"),

    // 401/403 - xác thực, phân quyền
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    FIELD_GROUP_FORBIDDEN(HttpStatus.FORBIDDEN, "Field group not permitted"),

    // 404/405/409/415
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    CONFLICT(HttpStatus.CONFLICT, "Resource state conflict"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type"),

    // 422
    FILE_TOO_LARGE(HttpStatus.UNPROCESSABLE_CONTENT, "File exceeds the maximum allowed size"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNPROCESSABLE_CONTENT, "Unsupported file type"),
    PASSWORD_SAME_AS_CURRENT(HttpStatus.UNPROCESSABLE_CONTENT, "New password must differ from the current password"),

    // 5xx
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Object storage operation failed"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Endpoint not implemented yet");

    private final HttpStatus status;
    private final String message;
}
