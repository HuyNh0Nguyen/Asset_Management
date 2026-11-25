import { ChangeDetectionStrategy, Component, computed, inject, signal, ViewContainerRef } from "@angular/core";
import { MatButton } from "@angular/material/button";
import { MatDialog } from "@angular/material/dialog";
import { MatIcon } from "@angular/material/icon";
import { RouterLink } from "@angular/router";
import { MeService } from "@api/services";
import { SessionStore } from "@core/auth/session.store";
import { skipErrorToast } from "@core/http/http-context";
import { DATA_SCOPE_LABELS, PERMISSION_LABELS } from "@core/i18n/labels.vi";
import { AppUrl } from "@core/routing/route.path";
import { MeMessage } from "../../me-messages";
import { AvatarSize } from "@shared/media/avatar-size";
import { openAvatarUpload } from "@shared/media/avatar-upload-dialog/avatar-upload-dialog";
import { Avatar } from "@shared/media/avatar/avatar";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { ConfirmTone } from "@shared/ui/confirm-dialog/confirm-dialog.model";
import { StatusChip } from "@shared/ui/status-chip/status-chip";
import { StatusTone } from "@shared/ui/status-chip/status-tone";
import { ToastService } from "@shared/ui/toast/toast.service";
import { filter, finalize, switchMap, tap } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

// Thông tin chỉ đọc (Admin sửa); người dùng chỉ tự đổi ảnh và mật khẩu
@Component({
    selector: 'app-my-account-page',
    imports: [RouterLink, MatButton, MatIcon, Avatar, StatusChip],
    templateUrl: './my-account-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyAccountPage {

    private readonly session = inject(SessionStore);
    private readonly meApi = inject(MeService);
    private readonly dialog = inject(MatDialog);
    private readonly viewContainerRef = inject(ViewContainerRef);
    private readonly toast = inject(ToastService);

    protected readonly me = this.session.me;
    protected readonly changePasswordUrl = AppUrl.CHANGE_PASSWORD;
    protected readonly avatarSize = AvatarSize.LARGE;
    protected readonly roleTone = StatusTone.INFO;
    protected readonly scopeTone = StatusTone.SUCCESS;
    protected readonly noValue = MeMessage.NO_VALUE;
    protected readonly removing = signal(false);

    // Quyền hiệu lực = hợp qua các vai trò; đổi vai trò thì khối này đổi ở lần chuyển trang kế tiếp
    protected readonly permissions = computed(() => (this.me()?.permissions ?? []).map((grant) => ({
        code: grant.code,
        label: PERMISSION_LABELS[grant.code],
        scope: DATA_SCOPE_LABELS[grant.scope],
    })));

    protected changeAvatar(): void {
        openAvatarUpload(this.dialog, {
            title: MeMessage.CHANGE_AVATAR_TITLE,
            upload: (file) => this.meApi.changeMyAvatar({ body: { file } }, skipErrorToast()),
        }, this.viewContainerRef)
            .pipe(filter(Boolean))
            .subscribe(() => {
                this.toast.success(MeMessage.AVATAR_CHANGED);
                // Header và trang này đổi ảnh ngay
                this.session.refreshMe();
            });
    }

    protected removeAvatar(): void {
        confirmAction(this.dialog, {
            title: MeMessage.REMOVE_AVATAR_TITLE,
            message: MeMessage.REMOVE_AVATAR_NOTICE,
            confirmLabel: MeMessage.REMOVE_AVATAR_CONFIRM,
            tone: ConfirmTone.DANGER,
        }, this.viewContainerRef).pipe(
            filter(Boolean),
            tap(() => this.removing.set(true)),
            switchMap(() => this.meApi.removeMyAvatar().pipe(finalize(() => this.removing.set(false)))),
        ).subscribe({
            next: () => {
                this.toast.success(MeMessage.AVATAR_REMOVED);
                this.session.refreshMe();
            },
            error: ignoreError,
        });
    }
}