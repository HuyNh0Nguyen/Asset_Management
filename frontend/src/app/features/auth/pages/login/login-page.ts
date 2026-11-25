import { HttpErrorResponse } from "@angular/common/http";
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel, MatSuffix } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { ActivatedRoute, Router } from "@angular/router";
import { ErrorCode } from "@api/models";
import { safeReturnUtl } from "@core/auth/return-url";
import { SessionStore } from "@core/auth/session.store";
import { parseErrorBody } from "@core/http/error-body";
import { describeHttpError } from "@core/http/http-error-message";
import { BrandImage } from "@core/layout/layout-breakpoint";
import { AppUrl, QueryParamKey } from "@core/routing/route.path";
import { AuthMessage } from "../../auth-mesages";
import { finalize, interval, Subscription } from "rxjs";

// 281 -> "4:41"
function formatCountdown(totalSeconds: number): string {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

@Component({
    selector: 'app-login-page',
    imports: [ReactiveFormsModule, MatFormField, MatLabel, MatError, MatSuffix, MatInput, MatIcon, MatButton, MatIconButton],
    templateUrl: './login-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {

    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly destroyRef = inject(DestroyRef);

    protected readonly brandLogo = BrandImage.LOGO;
    protected readonly forgotPasswordHint = AuthMessage.FORGOT_PASSWORD_HINT;
    protected readonly lockedMessage = AuthMessage.ACCOUNT_LOCKED_RETRY;
    protected readonly form = inject(NonNullableFormBuilder).group({
        username: ['', Validators.required],
        password: ['', Validators.required],
    });
    protected readonly submitting = signal(false);
    protected readonly errorMessage = signal<string | null>(null);
    protected readonly passwordVisible = signal(false);

    // Tạm khóa: Số giây còn lại do backend trả, đếm ngược theo mốc thời gian (tab ẩn bị hãm timer vẫn đúng)
    protected readonly lockSecondsLeft = signal(0);
    protected readonly locked = computed(() => this.lockSecondsLeft() > 0);
    protected readonly lockCountdown = computed(() => formatCountdown(this.lockSecondsLeft()));
    private lockTimer: Subscription | null = null;
    private lockedUsername: string | null = null;

    constructor() {
        // Khóa gắn với một tài khoản: Đổi tên đăng nhập thì bỏ khóa đang hiển thị
        this.form.controls.username.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
            if (this.lockedUsername !== null && value.trim() !== this.lockedUsername) {
                this.clearLock();
            }
        });
    }

    protected submit(): void {
        if (this.submitting() || this.locked()) {
            return;
        }
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const username = this.form.controls.username.value.trim();
        // PasswordPolicy không cho khoảng trắng: Chốt chặn cuối cho kéo-thả, tự điền, gõ thừa dấu cách
        const password = this.form.controls.password.value.trim();
        this.errorMessage.set(null);
        this.submitting.set(true);
        this.session.login(username, password)
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: (response) => void this.router.navigateByUrl(this.nextUrl(response.mustChangePassword)),
                // Lỗi đăng nhập hiện dưới form theo code, không toast
                error: (error: unknown) => this.onLoginFailed(username, error),
            });
    }

    protected togglePasswordVisible(): void {
        this.passwordVisible.update((visible) => !visible);
    }

    // Chép từ email có thể kèm "\r\n" ở đầu (Chrome đổi thành dấu cách khi dán): Bỏ ký tự trắng đầu/cuối trước khi chèn
    protected onPasswordPaste(event: ClipboardEvent): void {
        const text = event.clipboardData?.getData('text/plain');
        if (text === undefined) {
            return;
        }
        const cleaned = text.trim();
        if (cleaned === text) {
            return;
        }
        event.preventDefault();
        const input = event.target as HTMLInputElement;
        const start = input.selectionStart ?? input.value.length;
        const end = input.selectionEnd ?? input.value.length;
        input.setRangeText(cleaned, start, end, 'end');
        // setRangeText không phát sự kiện input: Báo cho form control cập nhật giá trị
        input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    // ACCOUNT_LOCKED kèm retryAfterSeconds -> đếm ngược; lỗi khác -> câu theo mã
    private onLoginFailed(username: string, error: unknown): void {
        const body = error instanceof HttpErrorResponse ? parseErrorBody(error.error) : null;
        const retryAfterSeconds = body?.code === ErrorCode.ACCOUNT_LOCKED ? body.data?.retryAfterSeconds : undefined;
        if (retryAfterSeconds && retryAfterSeconds > 0) {
            this.startLockCountdown(username, retryAfterSeconds);
            return;
        }
        this.errorMessage.set(describeHttpError(error));
    }

    private startLockCountdown(username: string, retryAfterSeconds: number): void {
        this.clearLock();
        this.lockedUsername = username;
        const unlockAt = Date.now() + retryAfterSeconds * 1000;
        const secondsLeft = (): number => Math.max(0, Math.ceil((unlockAt - Date.now()) / 1000));
        this.lockSecondsLeft.set(secondsLeft());
        this.lockTimer = interval(1000)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
                const left = secondsLeft();
                this.lockSecondsLeft.set(left);
                if (left === 0) {
                    this.clearLock();
                }
            });
    }

    private clearLock(): void {
        this.lockTimer?.unsubscribe();
        this.lockTimer = null;
        this.lockedUsername = null;
        this.lockSecondsLeft.set(0);
    }

    // Cờ bắt buộc đổi mật khẩu -> đổi trước khi vào hệ thống
    private nextUrl(mustChangePassword: boolean): string {
        if (mustChangePassword) {
            return AppUrl.CHANGE_PASSWORD;
        }
        return safeReturnUtl(this.route.snapshot.queryParamMap.get(QueryParamKey.RETURN_URL));
    }
}
