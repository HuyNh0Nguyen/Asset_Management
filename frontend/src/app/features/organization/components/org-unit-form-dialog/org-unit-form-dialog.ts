import { ChangeDetectionStrategy, Component, inject, signal } from "@angular/core";
import { FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatError, MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { ErrorCode, FieldErrorDetail, OrgUnitHeadResponse } from "@api/models";
import { ORG_CODE_MAX_LENGTH, ORG_CODE_PATTERN, ORG_NAME_MAX_LENGTH, OrgFormErrorKey } from "../../data/org-unit-ui";
import { OrgUnitsStore } from "../../data/org-units.store";
import { ToastService } from "@shared/ui/toast/toast.service";
import { OrgUnitFormDialogData, OrgUnitFormField, OrgUnitFormMode } from "./org-unit-form-dialog.model";
import { ORG_UNIT_TYPE_LABELS } from "@core/i18n/labels.vi";
import { OrgMessage } from "../../organization-messages";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { finalize, Observable } from "rxjs";
import { parseHttpError } from "@core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { describeHttpError } from "@core/http/http-error-message";
import { chosenUserValidator, UserPickerErrorKey, UserPickerValue } from "@shared/forms/user-picker/user-picker.model";
import { UserPicker } from "@shared/forms/user-picker/user-picker";
import { MatButton } from "@angular/material/button";

// Thêm: mã + tên, loại và cha chỉ đọc. Sửa: tên + trưởng đơn vị
@Component({
    selector: 'app-org-unit-form-dialog',
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
        MatButton,
        UserPicker,
    ],
    templateUrl: './org-unit-form-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrgUnitFormDialog {

    private readonly store = inject(OrgUnitsStore);
    private readonly toast = inject(ToastService);
    private readonly dialogRef = inject<MatDialogRef<OrgUnitFormDialog, boolean>>(MatDialogRef);
    private readonly data = inject<OrgUnitFormDialogData>(MAT_DIALOG_DATA);

    protected readonly isCreate = this.data.mode === OrgUnitFormMode.CREATE;
    protected readonly target = this.data.target;
    protected readonly childTypeLabel = ORG_UNIT_TYPE_LABELS[this.target.childUnitType];
    protected readonly codeHint = OrgMessage.CODE_FORMAT_HINT;
    protected readonly noHeadOption = OrgMessage.NO_HEAD_OPTION;
    protected readonly errorKey = OrgFormErrorKey;
    protected readonly codeMaxLength = ORG_CODE_MAX_LENGTH;
    protected readonly nameMaxLength = ORG_NAME_MAX_LENGTH;

    protected readonly form = inject(NonNullableFormBuilder).group({
        code: ['', [Validators.required, Validators.maxLength(ORG_CODE_MAX_LENGTH), Validators.pattern(ORG_CODE_PATTERN)]],
        name: [this.isCreate ? '' : this.target.name, [Validators.required, Validators.maxLength(ORG_NAME_MAX_LENGTH)]],
    });
    // null = để trống trưởng; gõ chữ mà không chọn -> chưa chọn (không hợp lệ)
    protected readonly headControl = new FormControl<UserPickerValue>(
        this.isCreate ? null : this.target.head ?? null,
        chosenUserValidator,
    );

    protected readonly submitting = signal(false);
    protected readonly loadingCandidates = signal(false);
    protected readonly headServerError = signal<string | null>(null);
    protected readonly candidates = signal<OrgUnitHeadResponse[]>([]);

    constructor() {
        // Đổi lựa chọn -> bỏ lỗi server cũ
        this.headControl.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.headServerError.set(null));
        if (this.isCreate) {
            return;
        }
        // Sửa: mã bất biến, không gửi
        this.form.controls.code.disable();
        this.loadCandidates();
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
        if (this.form.invalid || this.headControl.invalid || this.submitting()) {
            this.form.markAllAsTouched();
            this.headControl.markAsTouched();
            return;
        }
        this.submitting.set(true);
        this.save()
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => {
                    this.toast.success(this.isCreate ? OrgMessage.UNIT_CREATED : OrgMessage.UNIT_UPDATED);
                    this.dialogRef.close(true);
                },
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private save(): Observable<unknown> {
        const { code, name } = this.form.getRawValue();
        if (this.isCreate) {
            return this.store.create({ code, name, unitType: this.target.childUnitType, parentId: this.target.id });
        }
        return this.store.update(this.target.id, { name, headUserId: this.headControl.value?.id ?? null });
    }

    private loadCandidates(): void {
        this.loadingCandidates.set(true);
        this.store.headCandidates(this.target.id)
            .pipe(finalize(() => this.loadingCandidates.set(false)))
            .subscribe({ next: (list) => this.candidates.set(list), error: () => this.candidates.set([]) });
    }

    private onFailed(error: unknown): void {
        const { code, fieldErrors } = parseHttpError(error);
        switch (code) {
            case ErrorCode.ORG_UNIT_CODE_DUPLICATE:
                this.form.controls.code.setErrors({ [OrgFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.ORG_UNIT_HEAD_NOT_ACTIVE:
            case ErrorCode.ORG_UNIT_HEAD_OUTSIDE_SUBTREE:
                // Ứng viên vừa đổi -> báo dưới ô trưởng, nạp lại danh sách
                this.headServerError.set(ERROR_MESSAGES_VI[code]);
                this.headControl.setErrors({ [UserPickerErrorKey.SERVER]: true });
                this.headControl.markAsTouched();
                this.loadCandidates();
                return;
            case ErrorCode.VALIDATION_FAILED:
                this.applyFieldErrors(fieldErrors);
                return;
            case ErrorCode.CONFLICT:
            case ErrorCode.RESOURCE_NOT_FOUND:
                // Dữ liệu đã đổi ở nơi khác -> đóng, trang nạp lại cây
                this.toast.error(describeHttpError(error));
                this.dialogRef.close(true);
                return;
            default:
                this.toast.error(describeHttpError(error));
        }
    }

    // fieldErrors của 400 gắn vào ô; không hiển thị message tiếng Anh của server
    private applyFieldErrors(fieldErrors: FieldErrorDetail[]): void {
        for (const { field } of fieldErrors) {
            if (field === OrgUnitFormField.CODE) {
                this.form.controls.code.setErrors({ [OrgFormErrorKey.SERVER]: true });
            }
            if (field === OrgUnitFormField.NAME) {
                this.form.controls.name.setErrors({ [OrgFormErrorKey.SERVER]: true });
            }
        }
        this.form.markAllAsTouched();
    }
}