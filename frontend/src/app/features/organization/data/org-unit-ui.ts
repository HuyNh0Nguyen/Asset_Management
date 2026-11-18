import { OrgUnitAction, OrgUnitType } from "@api/models";

// Định dạng mã - khớp OrgCodeFormat ở backend (server vẫn là chuẩn)
export const ORG_CODE_PATTERN = '^[A-Z0-9_]+$';
export const ORG_CODE_MAX_LENGTH = 50;
export const ORG_NAME_MAX_LENGTH = 255;

export const ORG_UNIT_TYPE_ICONS: Record<OrgUnitType, string> = {
    [OrgUnitType.COMPANY]: 'domain',
    [OrgUnitType.BRANCH]: 'store',
    [OrgUnitType.DEPARTMENT]: 'groups',
};

export const ORG_UNIT_ACTION_ICONS: Record<OrgUnitAction, string> = {
    [OrgUnitAction.ADD_CHILD]: 'add',
    [OrgUnitAction.EDIT]: 'edit',
    [OrgUnitAction.MOVE]: 'drive_file_move',
    [OrgUnitAction.DELETE]: 'delete',
    [OrgUnitAction.DEACTIVATE]: 'block',
    [OrgUnitAction.REACTIVATE]: 'restart_alt',
};

// Thứ tựu trong menu thao tác - xóa luôn cuối
export const ORG_UNIT_ACTION_ORDER: readonly OrgUnitAction[] = [
    OrgUnitAction.ADD_CHILD,
    OrgUnitAction.EDIT,
    OrgUnitAction.MOVE,
    OrgUnitAction.DEACTIVATE,
    OrgUnitAction.REACTIVATE,
    OrgUnitAction.DELETE,
];

// Khóa lỗi gắn lên ô form
export enum OrgFormErrorKey {
    DUPLICATE = 'duplicate',
    SERVER = 'server',
    INVALID_SELECTION = 'invalidSelection',
}