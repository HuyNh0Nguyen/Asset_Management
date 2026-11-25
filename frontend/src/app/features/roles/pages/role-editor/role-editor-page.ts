import { ChangeDetectionStrategy, Component, computed, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MatError, MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { MatProgressBar } from "@angular/material/progress-bar";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { ErrorCode, FieldErrorDetail, RoleGrantRequest, RoleResponse } from "@api/models";
import { SessionStore } from "@core/auth/session.store";
import { describeHttpError } from "@core/http/http-error-message";
import { parseHttpError } from "@core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { AppUrl, RouteParamKey } from "@core/routing/route.path";
import { PermissionMatrix } from "../../components/permission-matrix/permission-matrix";
import { RoleFormErrorKey, RoleFormField } from "../../data/role-ui";
import { RolesStore } from "../../data/roles.store";
import { RoleMessage } from "../../roles-messages";
import { CODE_MAX_LENGTH, CODE_PATTERN, DESCRIPTION_MAX_LENGTH, NAME_MAX_LENGTH } from "@shared/forms/code-format";
import { ToastService } from "@shared/ui/toast/toast.service";
import { finalize, Observable } from "rxjs";

// Trang riêng (19 hàng không vừa hộp thoại): /roles/new và /roles/:id
@Component({
    selector: 'app-role-editor-page',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatFormField,
        MatLabel,
        MatHint,
        MatError,
        MatInput,
        MatButton,
        MatIcon,
        MatProgressBar,
        PermissionMatrix,
    ],
    templateUrl: './role-editor-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleEditorPage {

    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly store = inject(RolesStore);
    private readonly session = inject(SessionStore);
    private readonly toast = inject(ToastService);

    // Route 'new' không có :id
    private readonly roleId = this.route.snapshot.paramMap.get(RouteParamKey.ID);
    protected readonly isCreate = this.roleId === null;
    protected readonly listUrl = AppUrl.ROLES;
    protected readonly permissions = this.store.permissions;
    protected readonly role = signal<RoleResponse | null>(null);
    protected readonly loading = signal(!this.isCreate);
    protected readonly submitting = signal(false);
    protected readonly matrixError = signal<string | null>(null);
    protected readonly isSystem = computed(() => this.role()?.system ?? false);
    protected readonly systemNotice = RoleMessage.SYSTEM_LOCKED_NOTICE;
    protected readonly codeHint = RoleMessage.CODE_FORMAT_HINT;
    protected readonly errorKey = RoleFormErrorKey;
    protected readonly codeMaxLength = CODE_MAX_LENGTH;
    protected readonly nameMaxLength = NAME_MAX_LENGTH;
    protected readonly descriptionMaxLength = DESCRIPTION_MAX_LENGTH;

    protected readonly form = inject(NonNullableFormBuilder).group({
        code: ['', [Validators.required, Validators.maxLength(CODE_MAX_LENGTH), Validators.pattern(CODE_PATTERN)]],
        name: ['', [Validators.required, Validators.maxLength(NAME_MAX_LENGTH)]],
        description: ['', Validators.maxLength(DESCRIPTION_MAX_LENGTH)],
        grants: [[] as RoleGrantRequest[]],
    });

    constructor() {
        this.store.ensurePermissions();
        this.form.controls.grants.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.matrixError.set(null));
        if (this.roleId) {
            // Mã bất biến - gửi code khi sửa -> 400 UNKNOWN_FIELD
            this.form.controls.code.disable();
            this.loadRole(this.roleId);
        }
    }
    
    // Tự viết hoa khi gõ; server không tự viết hoa
    protected upperCaseCode(): void {
        const control = this.form.controls.code;
        const upper = control.value.toUpperCase();
        if (upper !== control.value) {
            control.setValue(upper);
        }
    }

    protected submit(): void {
        if (this.form.invalid || this.submitting() || this.isSystem()) {
            this.form.markAllAsTouched();
            return;
        }
        this.submitting.set(true);
        this.request()
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => this.onSaved(),
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private loadRole(id: string): void {
        this.store.get(id)
            .pipe(finalize(() => this.loading.set(false)))
            .subscribe({
                next: (role) => this.applyRole(role),
                // 404 đã được interceptor toast
                error: () => void this.router.navigateByUrl(AppUrl.ROLES),
            });
    }

    private applyRole(role: RoleResponse): void {
        this.role.set(role);
        this.form.patchValue({
            code: role.code,
            name: role.name,
            description: role.description ?? '',
            grants: role.grants.map((grant) => ({ permission: grant.permission, scope: grant.scope })),
        });
        // vai trò hệ thống chỉ xem
        if (role.system) {
            this.form.disable();
        }
    }

    private request(): Observable<RoleResponse> {
        const { code, name, description, grants } = this.form.getRawValue();
        const body = { name: name.trim(), description: description.trim() || undefined, grants };
        return this.roleId ? this.store.update(this.roleId, body) : this.store.create({ ...body, code });
    }

    // Không thu hồi token: người giữ (kể cả chính mình) nhận quyền mới ở thao tác kế tiếp
    private onSaved(): void {
        this.toast.success(RoleMessage.ROLE_SAVED);
        this.session.refreshMe();
        void this.router.navigateByUrl(AppUrl.ROLES);
    }

    private onFailed(error: unknown): void {
        const { code, fieldErrors } = parseHttpError(error);
        switch (code) {
            case ErrorCode.ROLE_CODE_DUPLICATE:
                this.form.controls.code.setErrors({ [RoleFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.ROLE_PERMISSION_SCOPE_INVALID:
            case ErrorCode.ROLE_PERMISSION_ADMIN_ONLY:
                this.matrixError.set(ERROR_MESSAGES_VI[code]);
                return;
            case ErrorCode.VALIDATION_FAILED:
                this.applyFieldErrors(fieldErrors);
                return;
            case ErrorCode.ROLE_SYSTEM_LOCKED:
            case ErrorCode.CONFLICT:
            case ErrorCode.RESOURCE_NOT_FOUND:
                this.toast.error(describeHttpError(error));
                void this.router.navigateByUrl(AppUrl.ROLES);
                return;
            default:
                this.toast.error(describeHttpError(error));
        }
    }

    // fieldErrors của 400 gắn vào ô; grantsUnique -> lỗi trên ma trận
    private applyFieldErrors(fieldErrors: FieldErrorDetail[]): void {
        const controls: Partial<Record<RoleFormField, AbstractControl>> = {
            [RoleFormField.CODE]: this.form.controls.code,
            [RoleFormField.NAME]: this.form.controls.name,
            [RoleFormField.DESCRIPTION]: this.form.controls.description,
        };
        for (const { field } of fieldErrors) {
            if (field === RoleFormField.GRANTS_UNIQUE) {
                this.matrixError.set(ERROR_MESSAGES_VI[ErrorCode.VALIDATION_FAILED]);
                continue;
            }
            controls[field as RoleFormField]?.setErrors({ [RoleFormErrorKey.SERVER]: true });
        }
        this.form.markAllAsTouched();
    }
}