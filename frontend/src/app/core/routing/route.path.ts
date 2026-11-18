// Đoạn đường dẫn dùng trong khai báo route
export enum RoutePath {
    LOGIN = 'login',
    CHANGE_PASSWORD = 'change-password',
    DEV_TOOLS = 'dev-tools',
    PROBES = 'probes',
    ATTRIBUTE_FORM = 'attribute-form',
    ORGANIZATION = 'organization',
    ORG_UNITS = 'org-units',
    RANKS = 'ranks',
}

// Đường dẫn tuyệt đối dùng khi điều hướng
export enum AppUrl {
    HOME = '/',
    LOGIN = '/login',
    CHANGE_PASSWORD = '/change-password',
    PROBES = '/dev-tools/probes',
    ATTRIBUTE_FORM = '/dev-tools/attribute-form',
    ORG_UNITS = '/organization/org-units',
    RANKS = '/organization/ranks',
}

// Khóa query param dùng chung
export enum QueryParamKey {
    RETURN_URL = 'returnUrl',
}

// Khóa route data dùng chung
export enum RouteDataKey {
    PERMISSION = 'permission',
}