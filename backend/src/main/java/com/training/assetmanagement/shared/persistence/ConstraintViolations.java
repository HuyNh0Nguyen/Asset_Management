package com.training.assetmanagement.shared.persistence;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;

// Nhận diện ràng buộc CSDL bị vi phạm theo tên (uk_..., fk_...) trong chuỗi nguyên nhân
public final class ConstraintViolations {

    public static boolean violates(DataIntegrityViolationException ex, String constraintName) {
        String needle = constraintName.toLowerCase(Locale.ROOT);
        Throwable cause = ex;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
            cause = cause.getCause() == cause ? null : cause.getCause();
        }
        return false;
    }

    private ConstraintViolations() {}
}
