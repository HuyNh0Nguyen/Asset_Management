import { FormControl, FormRecord } from "@angular/forms";
import { HeadReplacementRequest, OrgUnitRefResponse } from "@api/models";
import { chosenUserValidator, UserPickerValue } from "@shared/forms/user-picker/user-picker.model";

// Một control cho mỗi đơn vị cần chọn trưởng thay - khóa = id đơn vị
export type HeadReplacementForm = FormRecord<FormControl<UserPickerValue>>;

export function createHeadReplacementForm(): HeadReplacementForm {
    return new FormRecord<FormControl<UserPickerValue>>({});
}

// Đồng bộ control với danh sách đơn vị; đơn vị mới bắt đầu ở "chưa chọn"
export function syncHeadReplacementForm(form: HeadReplacementForm, units: readonly OrgUnitRefResponse[]): void {
    const unitIds = new Set(units.map((unit) => unit.id));
    Object.keys(form.controls)
        .filter((unitId) => !unitIds.has(unitId))
        .forEach((unitId) => form.removeControl(unitId));
    units
        .filter((unit) => !form.contains(unit.id))
        .forEach((unit) => form.addControl(unit.id, new FormControl<UserPickerValue>(undefined, chosenUserValidator)));
}

// "Để trống" -> bỏ newHeadUserId, backend hiểu là null
export function toHeadReplacements(form: HeadReplacementForm): HeadReplacementRequest[] {
    return Object.entries(form.getRawValue()).map(([unitId, value]) => ({ unitId, newHeadUserId: value?.id }));
}