import { ChangeDetectionStrategy, Component, effect, inject, signal, untracked, viewChild } from "@angular/core";
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatError, MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatCheckbox } from "@angular/material/checkbox";
import { MatOption, MatSelect } from "@angular/material/select";
import { MatButton } from "@angular/material/button";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { OrgUnitPicker } from "@shared/forms/org-unit-picker/org-unit-picker";
import { HeadReplacementPanel } from "../head-replacement-panel/head-replacement-panel";
import { UsersStore } from "../../data/users.store";
import { SessionStore } from "@core/auth/session.store";
import { ToastService } from "@shared/ui/toast/toast.service";
import { UserFormDialogData, UserFormMode } from "./user-form-dialog.model";
import { PHONE_PATTERN, USER_TEXT_MAX_LENGTH, UserFormErrorKey, UserFormField, USERNAME_PATTERN } from "../../data/user-ui";
import { userCreatedMessage, UserMessage } from "../../user-messages";
import { OrgUnitSelectable } from "@shared/forms/org-unit-picker/org-unit-picker.model";
import { ErrorCode, FieldErrorDetail, OrgUnitRefResponse, OrgUnitType, RoleResponse, UserResponse } from "@api/models";
import { createHeadReplacementForm, syncHeadReplacementForm, toHeadReplacements } from "../../data/head-replacement-form";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { catchError, distinctUntilChanged, filter, finalize, map, Observable, of, switchMap, tap } from "rxjs";
import { parseHttpError } from "@core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { describeHttpError } from "@core/http/http-error-message";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { ConfirmTone } from "@shared/ui/confirm-dialog/confirm-dialog.model";

// Thêm / sửa người dùng. Sửa: đổi đơn vị của một trưởng đơn vị -> bắt chọn trưởng thay
@Component({
    selector: 'app-user-form-dialog',
    imports: [
        ReactiveFormsModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatFormField,
        MatLabel,
        MatHint,
        MatError,
        MatInput,
        MatSelect,
        MatOption,
        MatCheckbox,
        MatButton,
        MatProgressSpinner,
        OrgUnitPicker,
        HeadReplacementPanel,
    ],
    templateUrl: './user-form-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormDialog {

    private readonly store = inject(UsersStore);
    private readonly session = inject(SessionStore);
    private readonly toast = inject(ToastService);
    private readonly dialog = inject(MatDialog);
    private readonly dialogRef = inject<MatDialogRef<UserFormDialog, boolean>>(MatDialogRef);
    private readonly data = inject<UserFormDialogData>(MAT_DIALOG_DATA);
    private readonly fb = inject(NonNullableFormBuilder);

    protected readonly isCreate = this.data.mode === UserFormMode.CREATE;
    protected readonly user = this.data.user ?? null;
    protected readonly orgUnits = this.store.orgUnits;
    protected readonly ranks = this.store.ranks;
    protected readonly roles = this.store.roles;
    protected readonly errorKey = UserFormErrorKey;
    protected readonly textMaxLength = USER_TEXT_MAX_LENGTH;
    protected readonly unitCheckLoading = UserMessage.UNIT_CHECK_LOADING;
    // Người dùng chỉ ngồi ở phòng ban
    protected readonly selectableUnit: OrgUnitSelectable = (node) => node.unitType === OrgUnitType.DEPARTMENT;

    protected readonly form = this.fb.group({
        username: ['', [Validators.required, Validators.pattern(USERNAME_PATTERN)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(USER_TEXT_MAX_LENGTH)]],
        fullName: [this.user?.fullName ?? '', [Validators.required, Validators.maxLength(USER_TEXT_MAX_LENGTH)]],
        phoneNumber: [this.user?.phoneNumber ?? '', Validators.pattern(PHONE_PATTERN)],
        orgUnitId: this.fb.control<string | null>(this.user?.orgUnit.id ?? null, Validators.required),
        rankId: [this.user?.rank.id ?? '', Validators.required],
        // Ít nhất một vai trò
        roleIds: this.fb.control<string[]>(this.user?.roles.map((role) => role.id) ?? [], Validators.required),
    });

    protected readonly headForm = createHeadReplacementForm();
    protected readonly unitsToReplace = signal<OrgUnitRefResponse[]>([]);
    protected readonly checkingUnits = signal(false);
    protected readonly headError = signal<string | null>(null);
    protected readonly orgUnitError = signal<string | null>(null);
    protected readonly rolesError = signal<string | null>(null);
    protected readonly submitting = signal(false);
    private readonly headPanel = viewChild(HeadReplacementPanel);

    constructor() {
        if (this.isCreate) {
            // Tích sẵn vai trò defaultForNewUser khi danh sách về - FE không tự biết mã EMPLOYEE
            effect(() => {
                const roles = this.roles();
                untracked(() => this.preselectDefaultRoles(roles));
            });
        } else {
            // username, email bất biến - không gửi
            this.form.controls.username.disable();
            this.form.controls.email.disable();
            this.watchUnitChange();
        }
        this.form.controls.orgUnitId.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.orgUnitError.set(null));
        this.form.controls.roleIds.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.rolesError.set(null));
    }

    // Tự viết thường khi gõ; server không tự viết thường
    protected lowerCaseUsername(): void {
        const control = this.form.controls.username;
        const lower = control.value.toLowerCase();
        if (lower !== control.value) {
            control.setValue(lower);
        }
    }

    protected isRoleChecked(roleId: string): boolean {
        return this.form.controls.roleIds.value.includes(roleId);
    }

    protected toggleRole(roleId: string, checked: boolean): void {
        const control = this.form.controls.roleIds;
        const others = control.value.filter((id) => id !== roleId);
        control.setValue(checked ? [...others, roleId] : others);
        control.markAsDirty();
        control.markAsTouched();
    }

    protected showRequired(control: AbstractControl): boolean {
        return control.hasError('required') && control.touched;
    }

    protected submit(): void {
        if (this.form.invalid || this.headForm.invalid || this.submitting() || this.checkingUnits()) {
            this.form.markAllAsTouched();
            this.headForm.markAllAsTouched();
            return;
        }
        if (this.losesOwnAdmin()) {
            confirmAction(this.dialog, {
                title: UserMessage.SELF_ADMIN_TITLE,
                message: UserMessage.SELF_ADMIN_NOTICE,
                confirmLabel: UserMessage.SELF_ADMIN_CONFIRM,
                tone: ConfirmTone.DANGER,
            }).pipe(filter(Boolean)).subscribe(() => this.save());
            return;
        }
        this.save();
    }

    private save(): void {
        this.submitting.set(true);
        this.headError.set(null);
        this.request()
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: (saved) => this.onSaved(saved),
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private request(): Observable<UserResponse> {
        const value = this.form.getRawValue();
        const common = {
            fullName: value.fullName.trim(),
            phoneNumber: value.phoneNumber.trim() || undefined,
            orgUnitId: value.orgUnitId ?? '',
            rankId: value.rankId,
            roleIds: value.roleIds,
        };
        if (!this.user) {
            return this.store.create({ ...common, username: value.username, email: value.email.trim() });
        }
        return this.store.update(this.user.id, { ...common, headReplacements: toHeadReplacements(this.headForm) });
    }

    private onSaved(saved: UserResponse): void {
        this.toast.success(this.isCreate ? userCreatedMessage(saved.email) : UserMessage.USER_UPDATED);
        // Sửa chính mình: tên / quyền trên header đổi ngay
        if (this.session.isSelf(saved.id)) {
            this.session.refreshMe();
        }
        this.dialogRef.close(true);
    }

    // Admin tự bỏ vai trò hệ thống của chính mình -> hỏi lại (nhận diện bằng cờ system, không bằng mã)
    private losesOwnAdmin(): boolean {
        const user = this.user;
        if (!user || !this.session.isSelf(user.id)) {
            return false;
        }
        const systemRoleIds = new Set(this.roles().filter((role) => role.system).map((role) => role.id));
        const hadSystemRole = user.roles.some((role) => systemRoleIds.has(role.id));
        const keepsSystemRole = this.form.controls.roleIds.value.some((roleId) => systemRoleIds.has(roleId));
        return hadSystemRole && !keepsSystemRole;
    }

    private preselectDefaultRoles(roles: RoleResponse[]): void {
        const control = this.form.controls.roleIds;
        if (control.dirty || control.value.length > 0) {
            return;
        }
        control.setValue(roles.filter((role) => role.defaultForNewUser).map((role) => role.id));
    }

    // Đổi đơn vị -> hỏi đơn vị nào cần chọn trưởng thay (kiểm trước, không khóa)
    private watchUnitChange(): void {
        this.form.controls.orgUnitId.valueChanges.pipe(
            distinctUntilChanged(),
            tap(() => this.checkingUnits.set(true)),
            switchMap((orgUnitId) => this.unitsToReplaceFor(orgUnitId)),
            takeUntilDestroyed(),
        ).subscribe((units) => {
            this.checkingUnits.set(false);
            this.applyUnits(units);
        });
    }

    private unitsToReplaceFor(orgUnitId: string | null): Observable<OrgUnitRefResponse[]> {
        const user = this.user;
        if (!user || !orgUnitId || orgUnitId === user.orgUnit.id) {
            return of([]);
        }
        return this.store.checkUnitChange(user.id, orgUnitId).pipe(
            map((result) => result.units),
            catchError((error: unknown) => {
                this.onFailed(error);
                return of([]);
            }),
        );
    }

    // Sau 422 HEAD_REPLACEMENT_MISMATCH: ai đó vừa gán người này làm trưởng thêm đơn vị
    private recheckUnits(): void {
        this.checkingUnits.set(true);
        this.unitsToReplaceFor(this.form.controls.orgUnitId.value)
            .pipe(finalize(() => this.checkingUnits.set(false)))
            .subscribe((units) => this.applyUnits(units));
    }

    private applyUnits(units: OrgUnitRefResponse[]): void {
        syncHeadReplacementForm(this.headForm, units);
        this.unitsToReplace.set(units);
    }

    private onFailed(error: unknown): void {
        const { code, fieldErrors } = parseHttpError(error);
        switch (code) {
            case ErrorCode.USERNAME_DUPLICATE:
                this.form.controls.username.setErrors({ [UserFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.EMAIL_DUPLICATE:
                this.form.controls.email.setErrors({ [UserFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.ORG_UNIT_INACTIVE:
            case ErrorCode.ORG_UNIT_TYPE_NOT_ALLOWED:
                // Đơn vị vừa ngừng hoạt động -> báo dưới ô, nạp lại cây
                this.orgUnitError.set(ERROR_MESSAGES_VI[code]);
                this.store.reloadOrgUnits();
                return;
            case ErrorCode.LAST_ACTIVE_ADMIN:
                this.rolesError.set(ERROR_MESSAGES_VI[code]);
                return;
            case ErrorCode.HEAD_REPLACEMENT_MISMATCH:
                this.headError.set(ERROR_MESSAGES_VI[code]);
                this.recheckUnits();
                return;
            case ErrorCode.ORG_UNIT_HEAD_NOT_ACTIVE:
            case ErrorCode.ORG_UNIT_HEAD_OUTSIDE_SUBTREE:
                this.headError.set(ERROR_MESSAGES_VI[code]);
                this.headPanel()?.reloadCandidates();
                return;
            case ErrorCode.VALIDATION_FAILED:
                this.applyFieldErrors(fieldErrors);
                return;
            case ErrorCode.USER_NOT_ACTIVE:
            case ErrorCode.CONFLICT:
            case ErrorCode.RESOURCE_NOT_FOUND:
                // Tài khoản / dữ liệu đã đổi ở nơi khác -> đóng, trang nạp lại
                this.toast.error(describeHttpError(error));
                this.dialogRef.close(true);
                return;
            default:
                this.toast.error(describeHttpError(error));
        }
    }

    // fieldErrors của 400 gắn vào ô; không hiển thị message tiếng Anh của server
    private applyFieldErrors(fieldErrors: FieldErrorDetail[]): void {
        const controls: Record<UserFormField, AbstractControl> = {
            [UserFormField.USERNAME]: this.form.controls.username,
            [UserFormField.EMAIL]: this.form.controls.email,
            [UserFormField.FULL_NAME]: this.form.controls.fullName,
            [UserFormField.PHONE_NUMBER]: this.form.controls.phoneNumber,
            [UserFormField.ORG_UNIT_ID]: this.form.controls.orgUnitId,
            [UserFormField.RANK_ID]: this.form.controls.rankId,
            [UserFormField.ROLE_IDS]: this.form.controls.roleIds,
        };
        for (const { field } of fieldErrors) {
            controls[field as UserFormField]?.setErrors({ [UserFormErrorKey.SERVER]: true });
        }
        this.form.markAllAsTouched();
    }
}