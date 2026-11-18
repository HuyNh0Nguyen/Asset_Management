import { AttributeDataType, CatalogStatus, OrgUnitAction, OrgUnitBlockerKind, OrgUnitStatus, OrgUnitType } from "@api/models";

// Nhãn tiếng Việt cho enum - khai bằng Record để thiếu nhãn là lỗi biên dịch
export const ATTRIBUTE_DATA_TYPE_LABELS: Record<AttributeDataType, string> = {
    [AttributeDataType.TEXT]: 'Văn bản',
    [AttributeDataType.NUMBER]: 'Số',
    [AttributeDataType.DATE]: 'Ngày',
    [AttributeDataType.BOOLEAN]: 'Có / Không',
    [AttributeDataType.SELECT]: 'Chọn từ danh sách',
};

export const CATALOG_STATUS_LABELS: Record<CatalogStatus, string> = {
    [CatalogStatus.ACTIVE]: 'Đang dùng',
    [CatalogStatus.INACTIVE]: 'Ngừng dùng',
};

export enum YesNoLabel {
    YES = 'Có',
    NO = 'Không',
};

export const ORG_UNIT_TYPE_LABELS: Record<OrgUnitType, string> = {
    [OrgUnitType.COMPANY]: 'Công ty',
    [OrgUnitType.BRANCH]: 'Chi nhánh',
    [OrgUnitType.DEPARTMENT]: 'Phòng ban',
}

export const ORG_UNIT_STATUS_LABELS: Record<OrgUnitStatus, string> = {
    [OrgUnitStatus.ACTIVE]: 'Đang hoạt động',
    [OrgUnitStatus.INACTIVE]: 'Ngừng hoạt động',
};

export const ORG_UNIT_ACTION_LABELS: Record<OrgUnitAction, string> = {
    [OrgUnitAction.ADD_CHILD]: 'Thêm đơn vị con',
    [OrgUnitAction.EDIT]: 'Sửa',
    [OrgUnitAction.MOVE]: 'Đổi đơn vị cha',
    [OrgUnitAction.DELETE]: 'Xóa',
    [OrgUnitAction.DEACTIVATE]: 'Ngừng hoạt động',
    [OrgUnitAction.REACTIVATE]: 'Kích hoạt lại',
};

export const ORG_UNIT_BLOCKER_LABELS: Record<OrgUnitBlockerKind, string> = {
    [OrgUnitBlockerKind.ACTIVE_USER]: 'Người dùng đang hoạt động',
    [OrgUnitBlockerKind.USER]: 'Người dùng (kể cả đã nghỉ)',
    [OrgUnitBlockerKind.ACTIVE_CHILD_UNIT]: 'Đơn vị con đang hoạt động',
    [OrgUnitBlockerKind.CHILD_UNIT]: 'Đơn vị con',
    [OrgUnitBlockerKind.LIVE_ASSET]: 'Tài sản chưa thanh lý',
    [OrgUnitBlockerKind.ASSET]: 'Tài sản',
    [OrgUnitBlockerKind.LIVE_BATCH]: 'Lô tài sản chưa thanh lý hết',
    [OrgUnitBlockerKind.BATCH]: 'Lô tài sản',
};