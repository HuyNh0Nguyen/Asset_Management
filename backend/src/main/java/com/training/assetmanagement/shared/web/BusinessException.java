package com.training.assetmanagement.shared.web;

import lombok.Getter;

// Chỉ nhận ErrorCode - status lấy từ mã, không truyền rời
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // detail chỉ để ghi log, không trả cho client
    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
