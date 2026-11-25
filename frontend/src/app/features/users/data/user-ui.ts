import { UserAction } from "@api/models";

export enum UserListColumn {
    USER = 'user',
    EMAIL = 'email',
    ORG_UNIT = 'orgUnit',
    RANK = 'rank',
    ROLES = 'roles',
    STATUS = 'status',
    ACTIONS = 'actions',
}

// Thuộc tính sort backend nhận (UserAttribute.sortable)
export enum UserSortField {
    USERNAME = 'username',
    FULL_NAME = 'fullName',
    CREATED_AT = 'createdAt',
}

// Cột theo bề rộng màn hình
export const DESKTOP_COLUMNS: readonly UserListColumn[] = [
    UserListColumn.USER, UserListColumn.EMAIL, UserListColumn.ORG_UNIT, UserListColumn.RANK,
    UserListColumn.ROLES, UserListColumn.STATUS, UserListColumn.ACTIONS,
];
export const TABLET_COLUMNS: readonly UserListColumn[] = [
    UserListColumn.USER, UserListColumn.ORG_UNIT, UserListColumn.ROLES, UserListColumn.STATUS, UserListColumn.ACTIONS,
];
export const PHONE_COLUMNS: readonly UserListColumn[] = [
    UserListColumn.USER, UserListColumn.STATUS, UserListColumn.ACTIONS,
];

export const USER_ACTION_ICONS: Record<UserAction, string> = {
    [UserAction.EDIT]: 'edit',
    [UserAction.RESET_PASSWORD]: 'lock_reset',
    [UserAction.CHANGE_AVATAR]: 'photo_camera',
    [UserAction.REMOVE_AVATAR]: 'hide_image',
    [UserAction.DEACTIVATE]: 'person_off',
    [UserAction.REACTIVATE]: 'person_check',
};

export const DANGER_USER_ACTIONS: ReadonlySet<UserAction> = new Set([UserAction.DEACTIVATE]);

// Khớp UserFieldFormat của backend - báo sớm, máy chủ là chuẩn
export const USERNAME_PATTERN = /^[a-z0-9._-]{3,50}$/;
export const PHONE_PATTERN = /^(\+?[0-9]{8,15})?$/;
export const USER_TEXT_MAX_LENGTH = 255;
export const SEARCH_DEBOUNCE_MS = 300;

// Tên trường trong fieldErrors của 400
export enum UserFormField {
    USERNAME = 'username',
    EMAIL = 'email',
    FULL_NAME = 'fullName',
    PHONE_NUMBER = 'phoneNumber',
    ORG_UNIT_ID = 'orgUnitId',
    RANK_ID = 'rankId',
    ROLE_IDS = 'roleIds',
}

export enum UserFormErrorKey {
    DUPLICATE = 'duplicate',
    SERVER = 'server',
}