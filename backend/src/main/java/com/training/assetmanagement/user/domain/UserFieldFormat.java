package com.training.assetmanagement.user.domain;

// Ràng buộc trường người dùng - khớp seed (admin, tgd.minh, qlts.giang) và độ dài cột
public final class UserFieldFormat {

    // Chữ thường, số, '.', '_', '-', 3-50 ký tự; server không tự viết thường
    public static final String USERNAME_PATTERN = "^[a-z0-9._-]{3,50}$";
    // Trống hoặc 8-15 chữ số, có thể có '+' đầu (vừa VARCHAR(20))
    public static final String PHONE_PATTERN = "^(\\+?[0-9]{8,15})?$";
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int FULL_NAME_MAX_LENGTH = 255;

    private UserFieldFormat() {}
}
