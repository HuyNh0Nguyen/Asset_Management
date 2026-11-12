import { AttributeDataType, CatalogStatus } from "@api/models";

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
}