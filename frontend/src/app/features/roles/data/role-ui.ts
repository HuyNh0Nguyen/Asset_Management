import { RoleAction } from "@api/models";

export enum RoleListView {
    LIST = 'LIST',
    MATRIX = 'MATRIX',
}

export enum RoleListColumn {
    NAME = 'name',
    CODE = 'code',
    DESCRIPTION = 'description',
    HOLDERS = 'holders',
    ACTIONS = 'actions',
}

export const DESKTOP_ROLE_COLUMNS: readonly RoleListColumn[] = [
    RoleListColumn.NAME, RoleListColumn.CODE, RoleListColumn.DESCRIPTION, RoleListColumn.HOLDERS, RoleListColumn.ACTIONS,
];
export const PHONE_ROLE_COLUMNS: readonly RoleListColumn[] = [
    RoleListColumn.NAME, RoleListColumn.HOLDERS, RoleListColumn.ACTIONS,
];

export const ROLE_ACTION_ICONS: Record<RoleAction, string> = {
    [RoleAction.EDIT]: 'edit',
    [RoleAction.DELETE]: 'delete',
};

// Tên trường trong fieldErrors của 400 (grantsUnique = @AssertTrue của request)
export enum RoleFormField {
    CODE = 'code',
    NAME = 'name',
    DESCRIPTION = 'description',
    GRANTS_UNIQUE = 'grantsUnique',
}

export enum RoleFormErrorKey {
    DUPLICATE = 'duplicate',
    SERVER = 'server',
}