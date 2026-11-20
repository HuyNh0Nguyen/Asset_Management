package com.training.assetmanagement.shared.web;

// Định dạng mã dùng chung: đơn vị, cấp bậc, vai trò, quy trình - khớp CHECK ..._code_format
public final class CodeFormat {

    // Chỉ chữ in hoa, chữ số, gạch dưới; server không tự viết hoa
    public static final String PATTERN = "^[A-Z0-9_]+$";
    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;

    private CodeFormat() {}
}
