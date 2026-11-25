import { Permission, PermissionDefinitionResponse } from "@api/models";

// Nhóm hiển thị của ma trận - chỉ là trình bày, không phải quy tắc
export enum PermissionGroup {
    ASSET = 'ASSET',
    REQUEST = 'REQUEST',
    MONITORING = 'MONITORING',
    ADMIN = 'ADMIN',
}

export const PERMISSION_GROUP_LABELS: Record<PermissionGroup, string> = {
    [PermissionGroup.ASSET]: 'Tài sản',
    [PermissionGroup.REQUEST]: 'Phiếu',
    [PermissionGroup.MONITORING]: 'Danh mục & giám sát',
    [PermissionGroup.ADMIN]: 'Quản trị',
};

// Record: thêm quyền mới mà quên xếp nhóm là lỗi biên dịch
export const PERMISSION_GROUP_OF: Record<Permission, PermissionGroup> = {
    [Permission.ASSET_VIEW]: PermissionGroup.ASSET,
    [Permission.ASSET_CREATE]: PermissionGroup.ASSET,
    [Permission.ASSET_TECH_EDIT]: PermissionGroup.ASSET,
    [Permission.ASSET_FINANCE_VIEW]: PermissionGroup.ASSET,
    [Permission.ASSET_FINANCE_EDIT]: PermissionGroup.ASSET,
    [Permission.ASSET_OPERATE]: PermissionGroup.ASSET,
    [Permission.ASSET_TRANSFER]: PermissionGroup.ASSET,
    [Permission.DISPOSAL_PROPOSE]: PermissionGroup.ASSET,
    [Permission.REQUEST_CREATE]: PermissionGroup.REQUEST,
    [Permission.REQUEST_VIEW]: PermissionGroup.REQUEST,
    [Permission.TASK_REASSIGN]: PermissionGroup.REQUEST,
    [Permission.CATALOG_MANAGE]: PermissionGroup.MONITORING,
    [Permission.DASHBOARD_VIEW]: PermissionGroup.MONITORING,
    [Permission.WORKFLOW_MANAGE]: PermissionGroup.MONITORING,
    [Permission.AUDIT_VIEW]: PermissionGroup.MONITORING,
    [Permission.ORG_MANAGE]: PermissionGroup.ADMIN,
    [Permission.USER_MANAGE]: PermissionGroup.ADMIN,
    [Permission.ROLE_MANAGE]: PermissionGroup.ADMIN,
    [Permission.SYSTEM_CONFIG]: PermissionGroup.ADMIN,
};

// SELF hợp lệ nhưng phủ rỗng - tác động lên tài sản (và tài chính) chỉ so đơn vị quản lý. Chỉ để ghi chú
export const SELF_COVERS_NOTHING: ReadonlySet<Permission> = new Set([
    Permission.ASSET_CREATE,
    Permission.ASSET_TECH_EDIT,
    Permission.ASSET_FINANCE_VIEW,
    Permission.ASSET_FINANCE_EDIT,
    Permission.ASSET_OPERATE,
    Permission.ASSET_TRANSFER,
    Permission.DISPOSAL_PROPOSE,
]);

export interface PermissionGroupView {
    group: PermissionGroup;
    label: string;
    items: PermissionDefinitionResponse[];
}

// Giữ thứ tự quyền như API (thứ tự enum của backend)
export function groupDefinitions(definitions: readonly PermissionDefinitionResponse[]): PermissionGroupView[] {
    return Object.values(PermissionGroup)
        .map((group) => ({
            group,
            label: PERMISSION_GROUP_LABELS[group],
            items: definitions.filter((definition) => PERMISSION_GROUP_OF[definition.code] === group),
        }))
        .filter((view) => view.items.length > 0);
}