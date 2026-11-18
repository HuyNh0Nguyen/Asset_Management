import { ChangeDetectionStrategy, Component, computed, inject, signal } from "@angular/core";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { OrgUnitsStore } from "../../data/org-units.store";
import { BlockerList } from "@shared/ui/blocker-list/blocker-list";
import { ToastService } from "@shared/ui/toast/toast.service";
import { OrgUnitBlockerPurpose, OrgUnitBlockersDialogData } from "./org-unit-blockers-dialog.model";
import { ErrorCode, OrgUnitBlockersResponse, OrgUnitStatus } from "@api/models";
import { OrgMessage } from "../../organization-messages";
import { BlockerGroup } from "@shared/ui/blocker-list/blocker-group.model";
import { finalize, Observable } from "rxjs";
import { toBlockerGroup } from "../../data/org-blocker-groups";
import { parseHttpError } from "@core/http/parse-http-error";
import { describeHttpError } from "@core/http/http-error-message";

// Mở là gọi ...-check: có vướng -> danh sách, không có nút xác nhận; trống -> câu xác nhận + nút
@Component({
    selector: 'app-org-unit-blockers-dialog',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton, MatProgressSpinner, BlockerList],
    templateUrl: './org-unit-blockers-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrgUnitBlockersDialog {

    private readonly store = inject(OrgUnitsStore);
    private readonly toast = inject(ToastService);
    private readonly dialogRef = inject<MatDialogRef<OrgUnitBlockersDialog, boolean>>(MatDialogRef);
    private readonly data = inject<OrgUnitBlockersDialogData>(MAT_DIALOG_DATA);

    protected readonly unit = this.data.unit;
    protected readonly isDelete = this.data.purpose === OrgUnitBlockerPurpose.DELETE;
    protected readonly title = this.isDelete ? 'Xóa đơn vị' : 'Ngừng hoạt động đơn vị';
    protected readonly verb = this.isDelete ? 'xóa' : 'ngừng hoạt động';
    // Xóa bị chặn mà đơn vị còn ACTIVE -> gợi ý ngừng hoạt động
    protected readonly suggestDeactivate = this.isDelete && this.unit.status === OrgUnitStatus.ACTIVE;
    protected readonly deactivateHint = OrgMessage.USE_DEACTIVATE_INSTEAD;

    protected readonly loading = signal(true);
    protected readonly submitting = signal(false);
    protected readonly groups = signal<BlockerGroup[] | null>(null);
    protected readonly canConfirm = computed(() => !this.loading() && this.groups()?.length === 0);

    constructor() {
        this.loadBlockers();
    }

    protected confirm(): void {
        if (!this.canConfirm() || this.submitting()) {
            return;
        }
        this.submitting.set(true);
        const command = this.isDelete ? this.store.delete(this.unit.id) : this.store.deactivate(this.unit.id);
        command
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => {
                    this.toast.success(this.isDelete ? OrgMessage.UNIT_DELETED : OrgMessage.UNIT_DEACTIVATED);
                    this.dialogRef.close(true);
                },
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private loadBlockers(): void {
        this.loading.set(true);
        this.check()
            .pipe(finalize(() => this.loading.set(false)))
            .subscribe({
                next: (response) => this.groups.set(response.blockers.map(toBlockerGroup)),
                // Lỗi đã được interceptor toast
                error: () => this.dialogRef.close(false),
            });
    }

    private check(): Observable<OrgUnitBlockersResponse> {
        return this.isDelete ? this.store.checkDeletion(this.unit.id) : this.store.checkDeactivation(this.unit.id);
    }

    private onFailed(error: unknown): void {
        const { code } = parseHttpError(error);
        if (code === ErrorCode.ORG_UNIT_NOT_EMPTY || code === ErrorCode.ORG_UNIT_IN_USE) {
            // Vừa có thứ mới vào đơn vị -> hiện danh sách vướng
            this.loadBlockers();
            return;
        }
        this.toast.error(describeHttpError(error));
        if (code === ErrorCode.CONFLICT || code === ErrorCode.RESOURCE_NOT_FOUND) {
            this.dialogRef.close(true);
        }
    }
}