import { AbstractControl, FormGroupDirective, NgForm } from "@angular/forms";
import { ErrorStateMatcher } from "@angular/material/core";
import { PasswordErrorKey } from "./password-policy";

// Ô xác nhận báo lỗi cả khi lỗi "không khớp" nằm ở cấp form
export class ConfirmPasswordErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean {
        if (!control) {
            return false;
        }
        const interacted = control.touched || !!form?.submitted;
        return interacted && (control.invalid || !!form?.form.hasError(PasswordErrorKey.MISMATCH));
    }
}