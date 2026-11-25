import { ChangeDetectionStrategy, Component, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormControl, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { ErrorCode, OrgUnitStatus, OrgUnitType } from "@api/models";
import { describeHttpError } from "@core/http/http-error-message";
import { parseHttpError } from "@core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { UserDialogData } from "../../data/user-list-query";
import { UsersStore } from "../../data/users.store";
import { oldUnitInactiveMessage, UserMessage } from "../../user-messages";
import { OrgUnitPicker } from "@shared/forms/org-unit-picker/org-unit-picker";
import { OrgUnitSelectable } from "@shared/forms/org-unit-picker/org-unit-picker.model";
import { ToastService } from "@shared/ui/toast/toast.service";
import { finalize } from "rxjs";

// Đơn vị cũ còn hoạt động -> điền sẵn, Admin chỉ bấm xác nhận; đã ngừng -> bắt chọn đơn vị mới
@Component({
    selector: 'app-user-reactivate-dialog',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton, OrgUnitPicker],
    templateUrl: './user-reactivate-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserReactivateDialog {

    private readonly store = inject(UsersStore);
    private readonly toast = inject(ToastService);
    private readonly dialogRef = inject<MatDialogRef<UserReactivateDialog, boolean>>(MatDialogRef);
    protected readonly user = inject<UserDialogData>(MAT_DIALOG_DATA).user;

    protected readonly oldUnitInactive = this.user.orgUnit.status === OrgUnitStatus.INACTIVE;
    protected readonly inactiveNotice = oldUnitInactiveMessage(this.user.orgUnit.name);
    protected readonly reactivateNote = UserMessage.REACTIVATE_NOTE;
    protected readonly nodes = this.store.orgUnits;
    protected readonly selectableUnit: OrgUnitSelectable = (node) => node.unitType === OrgUnitType.DEPARTMENT;
    protected readonly orgUnitControl = new FormControl<string | null>(
        this.oldUnitInactive ? null : this.user.orgUnit.id,
        Validators.required,
    );
    protected readonly orgUnitError = signal<string | null>(null);
    protected readonly submitting = signal(false);

    constructor() {
        this.orgUnitControl.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.orgUnitError.set(null));
    }

    protected showRequired(): boolean {
        return this.orgUnitControl.hasError('required') && this.orgUnitControl.touched;
    }

    protected submit(): void {
        const orgUnitId = this.orgUnitControl.value;
        if (!orgUnitId || this.submitting()) {
            this.orgUnitControl.markAsTouched();
            return;
        }
        this.submitting.set(true);
        this.store.reactivate(this.user.id, { orgUnitId })
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => {
                    this.toast.success(UserMessage.USER_REACTIVATED);
                    this.dialogRef.close(true);
                },
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private onFailed(error: unknown): void {
        const { code } = parseHttpError(error);
        if (code === ErrorCode.ORG_UNIT_INACTIVE || code === ErrorCode.ORG_UNIT_TYPE_NOT_ALLOWED) {
            // Đơn vị vừa ngừng hoạt động -> báo dưới ô, nạp lại cây
            this.orgUnitError.set(ERROR_MESSAGES_VI[code]);
            this.store.reloadOrgUnits();
            return;
        }
        this.toast.error(describeHttpError(error));
        this.dialogRef.close(true);
    }
}