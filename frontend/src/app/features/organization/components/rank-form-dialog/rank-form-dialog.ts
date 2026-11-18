import { ChangeDetectionStrategy, Component, inject, signal } from "@angular/core";
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatError, MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { RanksStore } from "../../data/ranks.store";
import { ToastService } from "@shared/ui/toast/toast.service";
import { RankFormDialogData, RankFormField } from "./rank-form-dialog.model";
import { OrgMessage } from "../../organization-messages";
import { ORG_CODE_MAX_LENGTH, ORG_CODE_PATTERN, ORG_NAME_MAX_LENGTH, OrgFormErrorKey } from "../../data/org-unit-ui";
import { finalize, Observable } from "rxjs";
import { parseHttpError } from "@core/http/parse-http-error";
import { ErrorCode, FieldErrorDetail } from "@api/models";
import { describeHttpError } from "@core/http/http-error-message";

// Số nguyên dương
const POSITIVE_INTEGER_PATTERN = '^[1-9][0-9]*$';

// Thêm: mã, tên, số thứ bậc. Sửa: tên, số thứ bậc (mã bất biến)
@Component({
    selector: 'app-rank-form-dialog',
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
    ],
    templateUrl: './rank-form-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RankFormDialog {

    private readonly store = inject(RanksStore);
    private readonly toast = inject(ToastService);
    private readonly dialogRef = inject<MatDialogRef<RankFormDialog, boolean>>(MatDialogRef);
    protected readonly rank = inject<RankFormDialogData>(MAT_DIALOG_DATA).rank;

    protected readonly isCreate = !this.rank;
    protected readonly codeHint = OrgMessage.CODE_FORMAT_HINT;
    protected readonly errorKey = OrgFormErrorKey;
    protected readonly codeMaxLength = ORG_CODE_MAX_LENGTH;
    protected readonly nameMaxLength = ORG_NAME_MAX_LENGTH;

    private readonly fb = inject(NonNullableFormBuilder);
    protected readonly form = this.fb.group({
        code: ['', [Validators.required, Validators.maxLength(ORG_CODE_MAX_LENGTH), Validators.pattern(ORG_CODE_PATTERN)]],
        name: [this.rank?.name ?? '', [Validators.required, Validators.maxLength(ORG_NAME_MAX_LENGTH)]],
        seniority: this.fb.control<number | null>(this.rank?.seniority ?? null,
            [Validators.required, Validators.pattern(POSITIVE_INTEGER_PATTERN)]
        ),
    });
    protected readonly submitting = signal(false);

    constructor() {
        if (!this.isCreate) {
            this.form.controls.code.disable();
        }
    }

    protected upperCaseCode(): void {
        const control = this.form.controls.code;
        const upper = control.value.toUpperCase();
        if (upper !== control.value) {
            control.setValue(upper);
        }
    }

    protected submit(): void {
        if (this.form.invalid || this.submitting()) {
            this.form.markAllAsTouched();
            return;
        }
        this.submitting.set(true);
        this.save()
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => {
                    this.toast.success(this.isCreate ? OrgMessage.RANK_CREATED : OrgMessage.RANK_UPDATED);
                    this.dialogRef.close(true);
                },
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private save(): Observable<unknown> {
        const { code, name, seniority } = this.form.getRawValue();
        const level = Number(seniority);
        return this.rank
            ? this.store.update(this.rank.id, { name, seniority: level })
            : this.store.create({ code, name, seniority: level });
    }

    private onFailed(error: unknown): void {
        const { code, fieldErrors } = parseHttpError(error);
        switch (code) {
            case ErrorCode.RANK_CODE_DUPLICATE:
                this.form.controls.code.setErrors({ [OrgFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.RANK_SENIORITY_DUPLICATE:
                this.form.controls.seniority.setErrors({ [OrgFormErrorKey.DUPLICATE]: true });
                return;
            case ErrorCode.VALIDATION_FAILED:
                this.applyFieldErrors(fieldErrors);
                return;
            case ErrorCode.CONFLICT:
            case ErrorCode.RESOURCE_NOT_FOUND:
                this.toast.error(describeHttpError(error));
                this.dialogRef.close(true);
                return;
            default:
                this.toast.error(describeHttpError(error));
        }
    }

    private applyFieldErrors(fieldErrors: FieldErrorDetail[]): void {
        const controls: Record<RankFormField, { setErrors(errors: Record<string, boolean>): void }> = {
            [RankFormField.CODE]: this.form.controls.code,
            [RankFormField.NAME]: this.form.controls.name,
            [RankFormField.SENIORITY]: this.form.controls.seniority,
        };
        for (const { field } of fieldErrors) {
            controls[field as RankFormField]?.setErrors({ [OrgFormErrorKey.SERVER]: true });
        }
        this.form.markAllAsTouched();
    }
}