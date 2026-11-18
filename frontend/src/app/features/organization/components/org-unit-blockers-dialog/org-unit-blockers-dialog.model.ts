import { OrgUnitNodeResponse } from "@api/models";

export enum OrgUnitBlockerPurpose {
    DELETE = 'DELETE',
    DEACTIVATE = 'DEACTIVATE',
}

export interface OrgUnitBlockersDialogData {
    unit: OrgUnitNodeResponse;
    purpose: OrgUnitBlockerPurpose;
}