import { OrgUnitNodeResponse } from "@api/models";

export enum OrgUnitFormMode {
    CREATE = 'CREATE',
    EDIT = 'EDIT',
}

// CREATE: target = đơn vị cha (loại con lấy từ childUnitType); EDIT: target = đơn vị đang sửa
export interface OrgUnitFormDialogData {
    mode: OrgUnitFormMode;
    target: OrgUnitNodeResponse;
}

// Tên trường trong fieldErrors của 400
export enum OrgUnitFormField {
    CODE = 'code',
    NAME = 'name',
}