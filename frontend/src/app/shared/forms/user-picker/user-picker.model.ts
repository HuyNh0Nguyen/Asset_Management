import { AbstractControl, ValidationErrors } from "@angular/forms";

// Ứng viên hiển thị
export interface UserPickerOption {
    id: string;
    username: string;
    fullName: string;
    orgUnitName?: string;
    avatarUrl?: string;
}

// undefined = chưa chọn; null = "Để trống" (tường minh); object = người được chọn
export type UserPickerValue = UserPickerOption | null | undefined;

export enum UserPickerErrorKey {
    NOT_CHOSEN = 'notChosen',
    SERVER = 'server',
}

export enum UserPickerText {
    EMPTY_OPTION = '- Để trống -',
    LOADING = 'Đang tải danh sách...',
}

// Bắt chọn từ danh sách (kể cả "Để trống")
export function chosenUserValidator(control: AbstractControl<UserPickerValue>): ValidationErrors | null {
    return control.value === undefined ? { [UserPickerErrorKey.NOT_CHOSEN]: true } : null;
}