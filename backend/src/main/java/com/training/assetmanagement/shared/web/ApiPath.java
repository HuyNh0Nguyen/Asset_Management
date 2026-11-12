package com.training.assetmanagement.shared.web;

// Đường dẫn HTTP dùng chung.
// Dùng hằng String (không dùng enum) vì annotation mapping chỉ nhận hằng lúc biên dịch.
public final class ApiPath {

    // ---------- Tiền tố, ký tự đại diện ----------
    public static final String API = "/api";
    public static final String ALL_SUBPATHS = "/**";

    // ---------- Gốc tài nguyên: Gắn ở @RequestMapping của controller ----------
    public static final String AUTH = API + "/auth";
    public static final String ME = API + "/me";
    public static final String ORG_UNITS = API + "/org-units";
    public static final String RANKS = API + "/ranks";
    public static final String USERS = API + "/users";
    public static final String ROLES = API + "/roles";
    public static final String PERMISSIONS = API + "/permissions";
    public static final String ASSET_TYPES = API + "/asset-types";
    public static final String DEV = API + "/dev";
    public static final String DEV_STORAGE_PROBES = DEV + "/storage/probes";
    public static final String DEV_EMAIL_PROBES = DEV + "/email/probes";

    // ---------- Đoạn con: Gắn ở @GetMapping/@PostMapping ----------
    // Xác thực
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String CHANGE_PASSWORD = "/change-password";

    // Danh mục
    public static final String ASSET_TYPE_ATTRIBUTES = "/{assetTypeId}/attributes";

    // Công cụ dev
    public static final String PROBE_URL = "/{probeId}/url";

    // Dùng chung cho các tài nguyên
    public static final String BY_ID = "/{id}";
    public static final String TREE = "/tree";
    public static final String DEACTIVATION_CHECK = BY_ID + "/deactivation-check";
    public static final String DEACTIVATE = BY_ID + "/deactivate";
    public static final String REACTIVATE = BY_ID + "/reactivate";
    public static final String RESET_PASSWORD = BY_ID + "/reset-password";
    public static final String AVATAR = "/avatar";
    public static final String USER_AVATAR = BY_ID + AVATAR;

    // ---------- Đường dẫn đầy đủ: Dùng ngoài controller (security, OpenAPI) ----------
    public static final String AUTH_LOGIN = AUTH + LOGIN;
    public static final String API_DOCS = API + "/v3/api-docs";
    public static final String SWAGGER_UI = API + "/swagger-ui";
    public static final String SWAGGER_UI_ENTRY = API + "/docs";
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ERROR = "/error";

    private ApiPath() {}
}
