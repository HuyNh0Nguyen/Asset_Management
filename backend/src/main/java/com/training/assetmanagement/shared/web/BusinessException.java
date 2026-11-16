package com.training.assetmanagement.shared.web;

import lombok.Getter;

// Chỉ nhận ErrorCode - status lấy từ mã, không truyền rời
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    // Số giây tới khi thử lại được (vd. tài khoản tạm khóa); null = không áp dụng
    private final Long retryAfterSeconds;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    // detail chỉ để ghi log, không trả cho client
    public BusinessException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null);
    }

    private BusinessException(ErrorCode errorCode, String detail, Long retryAfterSeconds) {
        super(detail);
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    // Lỗi có thời gian chờ: Số giây đi ra client trong ErrorDetail.retryAfterSeconds
    public static BusinessException retryAfter(ErrorCode errorCode, long retryAfterSeconds) {
        return new BusinessException(errorCode, errorCode.getMessage(), retryAfterSeconds);
    }
}
