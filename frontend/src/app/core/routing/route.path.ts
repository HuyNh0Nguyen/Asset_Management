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
    USERS = 'users',
    ROLES = 'roles',
    ROLE_NEW = 'new',
    ROLE_DETAIL = ':id',
    ME = 'me',
    NOTIFICATIONS = 'notifications',
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
    USERS = '/users',
    ROLES = '/roles',
    ROLE_NEW = '/roles/new',
    MY_ACCOUNT = '/me',
    NOTIFICATIONS = '/notifications',
}

// Khóa query param dùng chung
export enum QueryParamKey {
    RETURN_URL = 'returnUrl',
    SEARCH = 'q',
    ORG_UNIT_ID = 'orgUnitId',
    ROLE_ID = 'roleId',
    STATUS = 'status',
    PAGE = 'page',
    SIZE = 'size',
    SORT = 'sort',
    UNREAD = 'unread',
}

// Khóa route data dùng chung
export enum RouteDataKey {
    PERMISSION = 'permission',
}

// Khóa tham số đường dẫn
export enum RouteParamKey {
    ID = 'id',
}