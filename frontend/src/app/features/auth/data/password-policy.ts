import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

// Chép quy tắc PasswordPolicy của backend để báo sớm - server vẫn là chuẩn
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 72;

export enum PasswordErrorKey {
    POLICY = 'passwordPolicy',
    MISMATCH = 'passwordMismatch',
    SAME_AS_CURRENT = 'sameAsCurrent',
    SERVER = 'serverRejected',
}

// Tên trường trong form và trong fieldErrors của backend
export enum PasswordField {
    NEW_PASSWORD = 'newPassword',
    CONFIRM_PASSWORD = 'confirmPassword',
    PASSWORD_CONFIRMED = 'passwordConfirmed',
}

export enum PasswordRuleLabel {
    LENGTH = 'Từ 8 đến 72 ký tự',
    LOWERCASE = 'Có chữ thường',
    UPPERCASE = 'Có chữ hoa',
    DIGIT = 'Có chữ số',
    SPECIAL = 'Có ký tự đặc biệt',
    ASCII = 'Không dấu, không khoảng trắng',
}

export interface PasswordRule {
    label: PasswordRuleLabel;
    test: (value: string) => boolean;
}

export const PASSWORD_RULES: readonly PasswordRule[] = [
    { label: PasswordRuleLabel.LENGTH, test: (v) => v.length >= PASSWORD_MIN_LENGTH && v.length <= PASSWORD_MAX_LENGTH },
    { label: PasswordRuleLabel.LOWERCASE, test: (v) => /[a-z]/.test(v) },
    { label: PasswordRuleLabel.UPPERCASE, test: (v) => /[A-Z]/.test(v) },
    { label: PasswordRuleLabel.DIGIT, test: (v) => /\d/.test(v) },
    { label: PasswordRuleLabel.SPECIAL, test: (v) => /[^A-Za-z0-9]/.test(v) },
    // Chỉ ASCII in được  - tránh gõ nhầm bằng bộ gõ tiếng Việt
    { label: PasswordRuleLabel.ASCII, test: (v) => v.length > 0 && /^[\x21-\x7E]+$/.test(v) },
];

// Trống để required báo, tránh hai lỗi cho một ô
export const passwordPolicyValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '');
    if (!value) {
        return null;
    }
    return PASSWORD_RULES.every((rule) => rule.test(value)) ? null : { [PasswordErrorKey.POLICY]: true };
}

// Lỗi cắp form - ô xác nhận hiện qua ConfirmPasswordErrorStateMatcher
export const passwordsMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
    const newPassword = group.get(PasswordField.NEW_PASSWORD)?.value;
    const confirmPassword = group.get(PasswordField.CONFIRM_PASSWORD)?.value;
    if (!newPassword || !confirmPassword) {
        return null;
    }
    return newPassword === confirmPassword ? null : { [PasswordErrorKey.MISMATCH]: true };
};