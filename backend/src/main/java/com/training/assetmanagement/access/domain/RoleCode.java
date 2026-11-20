package com.training.assetmanagement.access.domain;

// Mã vai trò mà mã nguồn phải biết - mọi vai trò khác nhận diện bằng dữ liệu
public enum RoleCode {

    ADMIN,          // Vai trò hệ thống, khóa toàn bộ
    EMPLOYEE;       // Form tạo người dùng tích sẵn

    public boolean matches(String code) {
        return name().equals(code);
    }
}
