import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from "@angular/forms";
import { AttributeDataType, AttributeDefinitionResponse } from "@api/models";
import { AttributeValue, AttributeValues } from "./attribute-values.model";

// Sắp theo (displayOrder, id)
export function sortDefinitions(definitions: AttributeDefinitionResponse[]): AttributeDefinitionResponse[] {
    return [...definitions].sort((a, b) => a.displayOrder - b.displayOrder);
}

// Ô số: input type=number trả null khi gõ sai, nên chỉ cần chặn NaN / Infinity
const finiteNumber: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    return value === null || value === '' || Number.isFinite(value) ? null : { number: true };
}

export function createAttributeControl(
    definition: AttributeDefinitionResponse,
    value: AttributeValue | undefined,
    disabled: boolean,
): FormControl<AttributeValue | null> {
    const validators: ValidatorFn[] = [];
    if (definition.required) {
        validators.push(Validators.required);
    }
    if (definition.dataType === AttributeDataType.NUMBER) {
        validators.push(finiteNumber);
    }
    return new FormControl<AttributeValue | null>({ value: value ?? null, disabled });
}

// Bỏ khóa trống: thiếu khóa = "chưa nhập"; backend trộn với khóa của thuộc tính ngừng dùng
export function buildAttributesPayload(group: FormGroup): AttributeValues {
    const payload: AttributeValues = {};
    Object.entries(group.getRawValue() as Record<string, AttributeValue | null>).forEach(([code, value]) => {
        if (value !== null && value !== '') {
            payload[code] = value;
        }
    });
    return payload;
}