import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from "@angular/core";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { ErrorCode, OrgUnitRefResponse } from "@api/models";
import { BlockerList } from "@shared/ui/blocker-list/blocker-list";
import { HeadReplacementPanel } from "../head-replacement-panel/head-replacement-panel";
import { UsersStore } from "../../data/users.store";
import { SessionStore } from "@core/auth/session.store";
import { Router } from "@angular/router";
import { ToastService } from "@shared/ui/toast/toast.service";
import { UserDialogData } from "../../data/user-list-query";
import { UserMessage, withSessionNotice } from "../../user-messages";
import { BlockerGroup } from "@shared/ui/blocker-list/blocker-group.model";
import { createHeadReplacementForm, syncHeadReplacementForm, toHeadReplacements } from "../../data/head-replacement-form";
import { finalize } from "rxjs";
import { toUserBlockerGroup } from "../../data/user-blocker-groups";
import { AppUrl } from "@core/routing/route.path";
import { parseHttpError } from "@core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { describeHttpError } from "@core/http/http-error-message";

// Lỗi nghĩa là trạng thái vừa đổi -> kiểm lại, dựng lại hộp thoại
const RECHECK_ERRORS = new Set<ErrorCode>([
    ErrorCode.USER_DEACTIVATION_BLOCKED,
    ErrorCode.LAST_ACTIVE_ADMIN,
    ErrorCode.HEAD_REPLACEMENT_MISMATCH,
]);

// Mở là kiểm: có thứ chặn -> chỉ liệt kê + gợi ý, không có nút xác nhận.
// TODO: Không hứa "phiếu đang mở sẽ bị hủy" trước phase 6 - hộp thoại chỉ nói điều backend thật sự làm
@Component({
    selector: 'app-user-deactivate-dialog',
    imports: [
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatButton,
        MatProgressSpinner,
        BlockerList,
        HeadReplacementPanel,
    ],
    templateUrl: './user-deactivate-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserDeactivateDialog {

    private readonly store = inject(UsersStore);
    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);
    private readonly toast = inject(ToastService);
    private readonly dialogRef = inject<MatDialogRef<UserDeactivateDialog, boolean>>(MatDialogRef);
    protected readonly user = inject<UserDialogData>(MAT_DIALOG_DATA).user;

    private readonly isSelf = this.session.isSelf(this.user.id);
    protected readonly notice = withSessionNotice(UserMessage.DEACTIVATE_NOTICE, this.isSelf);
    protected readonly checking = signal(true);
    protected readonly groups = signal<BlockerGroup[]>([]);
    protected readonly blocked = computed(() => this.groups().length > 0);
    protected readonly headedUnits = signal<OrgUnitRefResponse[]>([]);
    protected readonly headForm = createHeadReplacementForm();
    protected readonly headError = signal<string | null>(null);
    protected readonly submitting = signal(false);
    private readonly headPanel = viewChild(HeadReplacementPanel);

    constructor() {
        this.check();
    }

    protected confirm(): void {
        if (this.headForm.invalid || this.submitting()) {
            this.headForm.markAllAsTouched();
            return;
        }
        this.submitting.set(true);
        this.headError.set(null);
        this.store.deactivate(this.user.id, { headReplacements: toHeadReplacements(this.headForm) })
            .pipe(finalize(() => this.submitting.set(false)))
            .subscribe({
                next: () => this.onDeactivated(),
                error: (error: unknown) => this.onFailed(error),
            });
    }

    // Kết quả chỉ để trình bày - lệnh thật luôn kiểm lại dưới khóa
    private check(): void {
        this.checking.set(true);
        this.store.checkDeactivation(this.user.id)
            .pipe(finalize(() => this.checking.set(false)))
            .subscribe({
                next: (result) => {
                    this.groups.set(result.blockers.map(toUserBlockerGroup));
                    syncHeadReplacementForm(this.headForm, result.headedUnits);
                    this.headedUnits.set(result.headedUnits);
                },
                // 409: đã vô hiệu hóa ở nơi khác - interceptor đã toast
                error: () => this.dialogRef.close(true),
            });
    }

    // Tự vô hiệu hóa: token của chính mình đã bị thu hồi -> xóa phiên
    private onDeactivated(): void {
        this.toast.success(UserMessage.USER_DEACTIVATED);
        if (this.isSelf) {
            this.dialogRef.close(false);
            this.session.clear();
            void this.router.navigateByUrl(AppUrl.LOGIN);
            return;
        }
        this.dialogRef.close(true);
    }

    private onFailed(error: unknown): void {
        const { code } = parseHttpError(error);
        if (code && RECHECK_ERRORS.has(code)) {
            this.toast.warning(ERROR_MESSAGES_VI[code]);
            this.check();
            return;
        }
        if (code === ErrorCode.ORG_UNIT_HEAD_NOT_ACTIVE || code === ErrorCode.ORG_UNIT_HEAD_OUTSIDE_SUBTREE) {
            this.headError.set(ERROR_MESSAGES_VI[code]);
            this.headPanel()?.reloadCandidates();
            return;
        }
        // 409 CONFLICT, 404...: đóng, trang nạp lại
        this.toast.error(describeHttpError(error));
        this.dialogRef.close(true);
    }
}