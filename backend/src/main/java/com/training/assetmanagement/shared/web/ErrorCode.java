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
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "Account is temporarily locked"),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "Account is disabled"),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "Session expired or revoked"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    FIELD_GROUP_FORBIDDEN(HttpStatus.FORBIDDEN, "Field group not permitted"),
    PASSWORD_CHANGE_REQUIRED(HttpStatus.FORBIDDEN, "Password change required before continuing"),

    // 404/405/409/415
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    CONFLICT(HttpStatus.CONFLICT, "Resource state conflict"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type"),
    ORG_UNIT_CODE_DUPLICATE(HttpStatus.CONFLICT, "Org unit code already exists"),
    RANK_CODE_DUPLICATE(HttpStatus.CONFLICT, "Rank code already exists"),
    RANK_SENIORITY_DUPLICATE(HttpStatus.CONFLICT, "Rank seniority already used"),
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "Username already exists"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "Email alreadt used by another account"),
    ROLE_CODE_DUPLICATE(HttpStatus.CONFLICT, "Role code already exists"),

    // 422
    FILE_TOO_LARGE(HttpStatus.UNPROCESSABLE_CONTENT, "File exceeds the maximum allowed size"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNPROCESSABLE_CONTENT, "Unsupported file type"),
    PASSWORD_SAME_AS_CURRENT(HttpStatus.UNPROCESSABLE_CONTENT, "New password must differ from the current password"),
    USER_NOT_ACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "User account is not active"),
    ORG_UNIT_COMPANY_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_CONTENT, "Cannot create another COMPANY org unit"),
    ORG_UNIT_INVALID_PARENT_TYPE(HttpStatus.UNPROCESSABLE_CONTENT, "Parent type does not match the org unit type"),
    ORG_UNIT_PARENT_CYCLE(HttpStatus.UNPROCESSABLE_CONTENT, "New parent is inside the moved subtree"),
    ORG_UNIT_PARENT_INACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "Parent org unit is inactive"),
    ORG_UNIT_ROOT_IMMUTABLE(HttpStatus.UNPROCESSABLE_CONTENT, "Root org unit cannot be changed this way"),
    ORG_UNIT_HEAD_NOT_ACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit head must be an active user"),
    ORG_UNIT_HEAD_OUTSIDE_SUBTREE(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit head must belong to the unit subtree"),
    ORG_UNIT_MOVE_DETACHES_HEAD(HttpStatus.UNPROCESSABLE_CONTENT, "Move would detach the head of a former ancestor"),
    ORG_UNIT_NOT_EMPTY(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit still has active users, units or assets"),
    ORG_UNIT_IN_USE(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit is still referenced"),
    ORG_UNIT_INACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit is inactive"),
    ORG_UNIT_TYPE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_CONTENT, "Org unit type not allowed here"),
    RANK_IN_USE(HttpStatus.UNPROCESSABLE_CONTENT, "Rank is still in use"),
    LAST_ACTIVE_ADMIN(HttpStatus.UNPROCESSABLE_CONTENT, "At least one active administrator must remain"),
    USER_DEACTIVATION_BLOCKED(HttpStatus.UNPROCESSABLE_CONTENT, "User still holds assets or is a named approver"),
    HEAD_REPLACEMENT_MISMATCH(HttpStatus.UNPROCESSABLE_CONTENT, "Head replacements do not match the units to replace"),
    ROLE_SYSTEM_LOCKED(HttpStatus.UNPROCESSABLE_CONTENT, "System role cannot be modified or deleted"),
    ROLE_PERMISSION_SCOPE_INVALID(HttpStatus.UNPROCESSABLE_CONTENT, "Scope not allowed for the permission"),
    ROLE_PERMISSION_ADMIN_ONLY(HttpStatus.UNPROCESSABLE_CONTENT, "Permission is reserved for the system role"),
    ROLE_IN_USE(HttpStatus.UNPROCESSABLE_CONTENT, "Role is still in use"),
    IMAGE_DIMENSIONS_TOO_LARGE(HttpStatus.UNPROCESSABLE_CONTENT, "Image pixel count exceeds the allowed limit"),

    // 5xx
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Object storage operation failed"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Endpoint not implemented yet");

    private final HttpStatus status;
    private final String message;
}
