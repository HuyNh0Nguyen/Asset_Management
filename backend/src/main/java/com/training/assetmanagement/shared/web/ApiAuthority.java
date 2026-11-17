package com.training.assetmanagement.shared.web;

// Biểu thức @PreAuthorize dùng chung.
// Dùng hằng String (không dùng enum) vì annotation chỉ nhận hằng lúc biên dịch - cùng lý do với ApiPath
public final class ApiAuthority {

    public static final String ORG_MANAGE = "hasAuthority('ORG_MANAGE')";
    public static final String USER_MANAGE = "hasAuthority('USER_MANAGE')";
    public static final String ORG_OR_USER_MANAGE = "hasAnyAuthority('ORG_MANAGE', 'USER_MANAGE')";

    private ApiAuthority() {}
}
