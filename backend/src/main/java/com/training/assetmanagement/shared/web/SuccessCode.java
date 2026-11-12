package com.training.assetmanagement.shared.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Mã của phong bì thành công
@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    SUCCESS("Request completed successfully");

    private final String message;
}
