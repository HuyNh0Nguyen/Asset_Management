import { HttpErrorResponse } from "@angular/common/http";
import { ChangeDetectionStrategy, Component, computed, inject, signal } from "@angular/core";
import { toSignal } from "@angular/core/rxjs-interop";
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel, MatSuffix } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { Router, RouterLink } from "@angular/router";
import { ErrorCode, FieldErrorDetail } from "@api/models";
import { AuthService } from "@api/services";
import { SessionStore } from "@core/auth/session.store";
import { parseErrorBody } from "@core/http/error-body";
import { skipErrorToast } from "@core/http/http-context";
import { describeHttpError } from "@core/http/http-error-message";
import { BrandImage } from "@core/layout/layout-breakpoint";
import { AppUrl } from "@core/routing/route.path";
import { AuthMessage } from "../../auth-mesages";
import { ConfirmPasswordErrorStateMatcher } from "../../data/confirm-password-error-state-matcher";
import { PASSWORD_RULES, PasswordErrorKey, PasswordField, passwordPolicyValidator, passwordsMatchValidator } from "../../data/password-policy";
import { ToastService } from "@shared/ui/toast/toast.service";
import { finalize } from "rxjs";

// Một trang cho cả đổi bắt buộc lẫn tự nguyện
@Component({
    selector: 'app-change-password-page',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatFormField,
        MatLabel,
        MatError,
        MatSuffix,
        MatInput,
        MatIcon,
        MatButton,
        MatIconButton,
    ],
    templateUrl: './change-password-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordPage {

    private readonly authApi = inject(AuthService);
    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);
    private readonly toast = inject(ToastService);

    protected readonly brandLogo = BrandImage.LOGO;
    protected readonly errorKey = PasswordErrorKey;
    protected readonly mandatoryNotice = AuthMessage.MANDATORY_CHANGE_NOTICE;
    protected readonly confirmMatcher = new ConfirmPasswordErrorStateMatcher();
    protected readonly username = computed(() => this.session.me()?.username ?? '');
    protected readonly mandatory = this.session.mustChangePassword;
    protected readonly homeUrl = this.session.homeUrl;

    protected readonly form = inject(NonNullableFormBuilder).group(
        {
            newPassword: ['', [Validators.required, passwordPolicyValidator]],
            confirmPassword: ['', Validators.required],
        },
        { validators: passwordsMatchValidator },
    );
    protected readonly submitting = signal(false);
    protected readonly loggingOut = signal(false);
    protected readonly newPasswordVisible = signal(false);

    // Đánh dấu từng quy tắc đã đạt khi người dùng gõ
    private readonly newPasswordValue = toSignal(this.form.controls.newPassword.valueChanges, { initialValue: '' });
    protected readonly ruleStates = computed(() => 
        PASSWORD_RULES.map((rule) => ({ label: rule.label, met: rule.test(this.newPasswordValue()) })),
    );

    protected submit(): void {
        if (this.form.invalid || this.submitting()) {
            this.form.markAllAsTouched();
            return;
        }
        this.submitting.set(true);
        this.authApi.changePassword({ body: this.form.getRawValue() }, skipErrorToast())
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => this.onChanged(),
                error: (error: unknown) => this.onFailed(error),
            });
    }

    protected toggleNewPasswordVisible(): void {
        this.newPasswordVisible.update((visible) => !visible);
    }

    protected logout(): void {
        this.loggingOut.set(true);
        this.session.logout()
            .pipe(finalize(() => this.loggingOut.set(false)))
            .subscribe(() => void this.router.navigateByUrl(AppUrl.LOGIN));
    }

    // Token hiện tại đã bị thu hồi -> xóa phiên, đăng nhập lại
    private onChanged(): void {
        this.toast.success(AuthMessage.PASSWORD_CHANGED);
        this.session.clear();
        void this.router.navigateByUrl(AppUrl.LOGIN);
    }

    private onFailed(error: unknown): void {
        const body = error instanceof HttpErrorResponse ? parseErrorBody(error.error) : null;
        if (body?.code === ErrorCode.PASSWORD_SAME_AS_CURRENT) {
            this.form.controls.newPassword.setErrors({ [PasswordErrorKey.SAME_AS_CURRENT]: true });
            return;
        }
        const fieldErrors = body?.code === ErrorCode.VALIDATION_FAILED ? body.data?.fieldErrors ?? [] : [];
        if (fieldErrors.length > 0) {
            this.applyFieldErrors(fieldErrors);
            return;
        }
        this.toast.error(describeHttpError(error));
    }

    // fieldErrors của 400 gắn vào ô; không hiển thị message tiếng Anh của server
    private applyFieldErrors(fieldErrors: FieldErrorDetail[]): void {
        for (const { field } of fieldErrors) {
            if (field === PasswordField.NEW_PASSWORD) {
                this.form.controls.newPassword.setErrors({ [PasswordErrorKey.SERVER]: true });
            }
            if (field === PasswordField.PASSWORD_CONFIRMED) {
                this.form.controls.confirmPassword.setErrors({ [PasswordErrorKey.MISMATCH]: true });
            }
            if (field === PasswordField.CONFIRM_PASSWORD) {
                this.form.controls.confirmPassword.setErrors({ required: true });
            }
        }
        this.form.markAllAsTouched();
    }
}