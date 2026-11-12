package com.training.assetmanagement.shared.security;

import com.training.assetmanagement.shared.web.ApiPath;

// Đường dẫn không cần đăng nhập
public final class PublicPaths {

    public static final String[] ALL = {
            ApiPath.AUTH_LOGIN,
            ApiPath.API_DOCS + ApiPath.ALL_SUBPATHS,
            ApiPath.SWAGGER_UI + ApiPath.ALL_SUBPATHS,
            ApiPath.SWAGGER_UI_ENTRY,
            // Controller dev chỉ tồn tại ở profile local/dev
            ApiPath.DEV + ApiPath.ALL_SUBPATHS,
            ApiPath.ACTUATOR_HEALTH,
            ApiPath.ERROR
    };

    private PublicPaths() {}
}
