import { ChangeDetectionStrategy, Component, computed, inject, signal, ViewContainerRef } from "@angular/core";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { OrgUnitsStore } from "../../data/org-units.store";
import { OrgUnitPicker } from "@shared/forms/org-unit-picker/org-unit-picker";
import { ToastService } from "@shared/ui/toast/toast.service";
import { OrgUnitMoveDialogData } from "./org-unit-move-dialog.model";
import { OrgUnitPickerMode } from "@shared/forms/org-unit-picker/org-unit-picker.model";
import { BlockingHeadResponse, ErrorCode, OrgUnitAction, OrgUnitNodeResponse, OrgUnitType } from "@api/models";
import { descendantIds } from "@shared/forms/org-unit-picker/org-tree.utils";
import { catchError, finalize, map, Observable, of, Subject, switchMap } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { OrgUnitFormDialog } from "../org-unit-form-dialog/org-unit-form-dialog";
import { OrgUnitFormDialogData, OrgUnitFormMode } from "../org-unit-form-dialog/org-unit-form-dialog.model";
import { appDialogConfig, DialogWidth } from "@shared/ui/dialog/app-dialog-config";
import { OrgMessage } from "../../organization-messages";
import { parseHttpError } from "@core/http/parse-http-error";
import { describeHttpError } from "@core/http/http-error-message";

// Chọn cha mới -> move-check -> danh sách trưởng vướng HOẶC nút Chuyển
@Component({
    selector: 'app-org-unit-move-dialog',
    imports: [
        ReactiveFormsModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatButton,
        MatProgressSpinner,
        OrgUnitPicker,
    ],
    templateUrl: './org-unit-move-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrgUnitMoveDialog {

    private readonly store = inject(OrgUnitsStore);
    private readonly toast = inject(ToastService);
    private readonly dialog = inject(MatDialog);
    private readonly viewContainerRef = inject(ViewContainerRef);
    private readonly dialogRef = inject<MatDialogRef<OrgUnitMoveDialog, boolean>>(MatDialogRef);

    protected readonly unit = inject<OrgUnitMoveDialogData>(MAT_DIALOG_DATA).unit;
    protected readonly nodes = this.store.nodes;
    // Chỉ nạp và hiển thị danh sách cây khi người dùng chọn vào ô tìm kiếm
    protected readonly initialized = signal(false);
    protected readonly pickerNodes = computed(() => this.initialized() ? this.nodes() : []);
    protected readonly pickerMode = OrgUnitPickerMode.WRITE;
    protected readonly parentControl = new FormControl<string | null>(null);

    protected readonly checking = signal(false);
    protected readonly moving = signal(false);
    // null = chưa kiểm; [] = chuyển được
    protected readonly blockingHeads = signal<BlockingHeadResponse[] | null>(null);
    protected readonly hasBlockingHeads = computed(() => (this.blockingHeads()?.length ?? 0) > 0);
    protected readonly canMove = computed(() =>
        this.blockingHeads()?.length === 0 && !this.checking() && !this.moving());

    // Không nhận chính nó, con cháu, cha hiện tại; chỉ đơn vị nhận được phòng ban con.
    // Lọc bằng allowedActions + childUnitType từ server
    private readonly excludedIds = computed(() =>
        new Set([this.unit.id, this.unit.parentId ?? '', ...descendantIds(this.nodes(), this.unit.id)]));
    protected readonly selectable = (node: OrgUnitNodeResponse): boolean =>
        !this.excludedIds().has(node.id)
        && node.allowedActions.includes(OrgUnitAction.ADD_CHILD)
        && node.childUnitType === OrgUnitType.DEPARTMENT;

    private readonly checkRequests = new Subject<string | null>();

    constructor() {
        // Mỗi lần đổi cha mới: hủy lần kiểm cũ, kiểm lại
        this.checkRequests
            .pipe(
                switchMap((parentId) => this.check(parentId)),
                takeUntilDestroyed(),
            )
            .subscribe((heads) => this.blockingHeads.set(heads));
        this.parentControl.valueChanges
            .pipe(takeUntilDestroyed())
            .subscribe((parentId) => this.checkRequests.next(parentId));
    }

    protected initPickerNodes(): void {
        if (!this.initialized()) {
            this.initialized.set(true);
        }
    }

    // Mở hộp thoại Sửa của đơn vị vướng; lưu xong thì nạp lại cây và kiểm lại
    protected editHead(item: BlockingHeadResponse): void {
        const target = this.store.byId().get(item.unitId);
        if (!target) {
            return;
        }
        this.dialog
            .open<OrgUnitFormDialog, OrgUnitFormDialogData, boolean>(OrgUnitFormDialog,
                appDialogConfig({ mode: OrgUnitFormMode.EDIT, target }, DialogWidth.MEDIUM, this.viewContainerRef)
            )
            .afterClosed()
            .subscribe((saved) => {
                if (saved) {
                    this.store.load();
                    this.checkRequests.next(this.parentControl.value);
                }
            });
    }

    protected move(): void {
        const parentId = this.parentControl.value;
        if (!parentId || !this.canMove()) {
            return;
        }
        this.moving.set(true);
        this.store.move(this.unit.id, parentId)
            .pipe(finalize(() => this.moving.set(false)))
            .subscribe({
                next: () => {
                    this.toast.success(OrgMessage.UNIT_MOVED);
                    this.dialogRef.close(true);
                },
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private check(parentId: string | null): Observable<BlockingHeadResponse[] | null> {
        this.blockingHeads.set(null);
        if (!parentId) {
            return of(null);
        }
        this.checking.set(true);
        return this.store.checkMove(this.unit.id, parentId).pipe(
            map((response) => response.blockingHeads),
            // Lỗi đã được interceptor toast
            catchError(() => of(null)),
            finalize(() => this.checking.set(false)),
        );
    }

    private onFailed(error: unknown): void {
        const { code } = parseHttpError(error);
        if (code === ErrorCode.ORG_UNIT_MOVE_DETACHES_HEAD) {
            // Vừa có người đổi trưởng -> hiện lại danh sách vướng
            this.checkRequests.next(this.parentControl.value);
            return;
        }
        this.toast.error(describeHttpError(error));
        if (code === ErrorCode.CONFLICT || code === ErrorCode.RESOURCE_NOT_FOUND) {
            this.dialogRef.close(true);
        }
    }
}
