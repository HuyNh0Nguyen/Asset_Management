package com.training.assetmanagement.auth.domain;

// Quy tắc độ mạnh mật khẩu; dùng chung cho đổi mật khẩu và mật khẩu tạm
public class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    // BCrypt chỉ băm 72 byte đầu
    public static final int MAX_LENGTH = 72;

    // Có chữ thường, chữ hoa, chữ số, ký tự đặc biệt.
    // Chỉ ký tự ASCII in được: 72 ký tự = 72 byte, tránh gõ nhầm dấu bằng bộ gõ tiếng Việt
    public static final String PATTERN =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])[\\x21-\\x7E]+$";

    private PasswordPolicy() {}
}
