package com.training.assetmanagement.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.Clock;
import java.util.List;

// Mọi ngoại lệ trong controller / service -> phong bì lỗi
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        if (ex.getErrorCode().getStatus().is5xxServerError()) {
            log.error("Error {} at {}: {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage(), ex);
        }
        return build(ex.getErrorCode(), request, null);
    }

    // 400 do @Valid trên body - chỉ ở đây mới có fieldErrors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleInvalidBody(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
            .toList();
        return build(ErrorCode.VALIDATION_FAILED, request, fieldErrors);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleInvalidParameter(Exception ex, HttpServletRequest request) {
        return build(ErrorCode.VALIDATION_FAILED, request, null);
    }

    // Trường lạ -> 400; phase 3 tách nhánh "trường thuộc nhóm khác" -> 403 FIELD_GROUP_FORBIDDEN
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUnreadable(HttpMessageNotReadableException ex,
                                                                     HttpServletRequest request) {
        ErrorCode code = ex.getCause() instanceof UnrecognizedPropertyException
            ? ErrorCode.UNKNOWN_FIELD
            : ErrorCode.MALFORMED_REQUEST;
        return build(code, request, null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBadParameter(Exception ex, HttpServletRequest request) {
        return build(ErrorCode.MALFORMED_REQUEST, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleNotFound(NoResourceFoundException ex,
                                                                   HttpServletRequest request) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMethod(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleTooLarge(MaxUploadSizeExceededException ex,
                                                                   HttpServletRequest request) {
        return build(ErrorCode.FILE_TOO_LARGE, request, null);
    }

    // @PreAuthorize từ chối: Ném trong controller nên tới đây, không tới RestAccessDeniedHandler
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleAccessDenied(AccessDeniedException ex,
                                                                       HttpServletRequest request) {
        return build(ErrorCode.FORBIDDEN, request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleAuthentication(AuthenticationException ex,
                                                                         HttpServletRequest request) {
        return build(ErrorCode.UNAUTHORIZED, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(ErrorCode.INTERNAL_ERROR, request, null);
    }

    private ResponseEntity<ApiResponse<ErrorDetail>> build(ErrorCode code, HttpServletRequest request,
                                                           List<FieldErrorDetail> fieldErrors) {
        ErrorDetail detail = ErrorDetail.of(code, request.getRequestURI(), clock, fieldErrors);
        return ResponseEntity.status(code.getStatus()).body(ApiResponse.error(code, detail));
    }
}
