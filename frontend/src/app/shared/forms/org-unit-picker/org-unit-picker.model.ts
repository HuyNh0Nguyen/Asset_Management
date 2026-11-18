import { OrgUnitNodeResponse } from "@api/models";

// WRITE: bỏ mọi đơn vị ngừng hoạt động (bộ chọn khi ghi); FILTER: giữ lại cho tra cứu
export enum OrgUnitPickerMode {
    WRITE = 'WRITE',
    FILTER = 'FILTER',
}

export type OrgUnitSelectable = (node: OrgUnitNodeResponse) => boolean;

export interface OrgUnitPickerOption {
    node: OrgUnitNodeResponse;
    selectable: boolean;
}