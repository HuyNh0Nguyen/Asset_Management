package com.training.assetmanagement.organization.domain;

// Định dạng mã đơn vị / cấp bậc - khớp ck_org_units_code_format, ck_ranks_code_format ở V1
public final class OrgCodeFormat {

    // Chỉ chữ in hoa, chữ số, gạch dưới; server không tự viết hoa
    public static final String PATTERN = "^[A-Z0-9_]+$";
    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 255;

    private OrgCodeFormat() {}
}
