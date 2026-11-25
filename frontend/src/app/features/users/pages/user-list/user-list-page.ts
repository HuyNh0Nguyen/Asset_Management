import { BreakpointObserver } from "@angular/cdk/layout";
import { ComponentType } from "@angular/cdk/portal";
import { ChangeDetectionStrategy, Component, computed, effect, inject, untracked, ViewContainerRef } from "@angular/core";
import { takeUntilDestroyed, toSignal } from "@angular/core/rxjs-interop";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatOption } from "@angular/material/core";
import { MatDialog } from "@angular/material/dialog";
import { MatFormField, MatLabel, MatPrefix } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { MatMenu, MatMenuContent, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { PageEvent } from "@angular/material/paginator";
import { MatSelect } from "@angular/material/select";
import { MatSortModule, Sort } from "@angular/material/sort";
import { MatTableModule } from "@angular/material/table";
import { ActivatedRoute, Router } from "@angular/router";
import { OrgUnitStatus, UserAction, UserResponse, UserStatus } from "@api/models";
import { SessionStore } from "@core/auth/session.store";
import { USER_ACTION_LABELS, USER_STATUS_LABELS } from "@core/i18n/labels.vi";
import { ViewportQuery } from "@core/layout/layout-breakpoint";
import { AppUrl } from "@core/routing/route.path";
import { UserDeactivateDialog } from "../../components/user-deactivate-dialog/user-deactivate-dialog";
import { UserFormDialog } from "../../components/user-form-dialog/user-form-dialog";
import { UserFormMode } from "../../components/user-form-dialog/user-form-dialog.model";
import { UserReactivateDialog } from "../../components/user-reactivate-dialog/user-reactivate-dialog";
import { readUserListQuery, toUserListParams, UserListQuery } from "../../data/user-list-query";
import { DANGER_USER_ACTIONS, DESKTOP_COLUMNS, PHONE_COLUMNS, SEARCH_DEBOUNCE_MS, TABLET_COLUMNS, USER_ACTION_ICONS, UserListColumn, UserSortField } from "../../data/user-ui";
import { UsersStore } from "../../data/users.store";
import { UserMessage, withSessionNotice } from "../../user-messages";
import { OrgUnitPicker } from "@shared/forms/org-unit-picker/org-unit-picker";
import { OrgUnitPickerMode } from "@shared/forms/org-unit-picker/org-unit-picker.model";
import { AvatarSize } from "@shared/media/avatar-size";
import { openAvatarUpload } from "@shared/media/avatar-upload-dialog/avatar-upload-dialog";
import { Avatar } from "@shared/media/avatar/avatar";
import { parseSort, toSortParam } from "@shared/table/server-table/page-query.model";
import { ServerTable } from "@shared/table/server-table/server-table";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { ConfirmTone } from "@shared/ui/confirm-dialog/confirm-dialog.model";
import { appDialogConfig, DialogWidth } from "@shared/ui/dialog/app-dialog-config";
import { StatusChip } from "@shared/ui/status-chip/status-chip";
import { StatusTone } from "@shared/ui/status-chip/status-tone";
import { ToastService } from "@shared/ui/toast/toast.service";
import { debounceTime, distinctUntilChanged, filter, map, switchMap } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

@Component({
    selector: 'app-user-list-page',
    imports: [
        ReactiveFormsModule,
        MatTableModule,
        MatSortModule,
        MatButton,
        MatIconButton,
        MatIcon,
        MatMenu,
        MatMenuItem,
        MatMenuTrigger,
        MatMenuContent,
        MatFormField,
        MatLabel,
        MatPrefix,
        MatInput,
        MatSelect,
        MatOption,
        OrgUnitPicker,
        ServerTable,
        StatusChip,
        Avatar,
    ],
    templateUrl: './user-list-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserListPage {

    private readonly store = inject(UsersStore);
    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly dialog = inject(MatDialog);
    private readonly viewContainerRef = inject(ViewContainerRef);
    private readonly toast = inject(ToastService);
    private readonly breakpoints = inject(BreakpointObserver);

    protected readonly users = this.store.users;
    protected readonly total = this.store.total;
    protected readonly loading = this.store.loading;
    protected readonly orgUnits = this.store.orgUnits;
    protected readonly roles = this.store.roles;

    protected readonly column = UserListColumn;
    protected readonly sortField = UserSortField;
    protected readonly statuses = Object.values(UserStatus);
    protected readonly statusLabels = USER_STATUS_LABELS;
    protected readonly avatarSize = AvatarSize.SMALL;
    protected readonly filterMode = OrgUnitPickerMode.FILTER;
    protected readonly inactiveStatus = UserStatus.INACTIVE;
    protected readonly inactiveUnit = OrgUnitStatus.INACTIVE;
    protected readonly roleTone = StatusTone.INFO;

    // Query string là nguồn duy nhất của bộ lọc, trang, sắp xếp
    protected readonly query = toSignal(this.route.queryParamMap.pipe(map(readUserListQuery)), { requireSync: true });
    protected readonly sort = computed(() => parseSort(this.query().sort));

    protected readonly searchControl = new FormControl(this.query().q, { nonNullable: true });
    protected readonly orgUnitControl = new FormControl<string | null>(this.query().orgUnitId);
    protected readonly roleControl = new FormControl<string | null>(this.query().roleId);
    protected readonly statusControl = new FormControl<UserStatus | null>(this.query().status);

    // iPad / điện thoại: ẩn bớt cột
    private readonly isPhone = toSignal(
        this.breakpoints.observe(ViewportQuery.PHONE).pipe(map((state) => state.matches)), { initialValue: false}
    );
    private readonly isTablet = toSignal(
        this.breakpoints.observe(ViewportQuery.TABLET).pipe(map((state) => state.matches)), { initialValue: false }
    );
    protected readonly displayedColumns = computed(() =>
        this.isPhone() ? PHONE_COLUMNS : this.isTablet() ? TABLET_COLUMNS : DESKTOP_COLUMNS
    );

    constructor() {
        this.store.loadReferences();
        // Query đổi (lọc, trang, sắp xếp, F5) -> nạp lại
        effect(() => {
            const query = this.query();
            untracked(() => this.store.load(query));
        });
        this.searchControl.valueChanges
            .pipe(debounceTime(SEARCH_DEBOUNCE_MS), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((q) => this.navigate({ q }));
        this.orgUnitControl.valueChanges.pipe(takeUntilDestroyed()).subscribe((orgUnitId) => this.navigate({ orgUnitId }));
        this.roleControl.valueChanges.pipe(takeUntilDestroyed()).subscribe((roleId) => this.navigate({ roleId }));
        this.statusControl.valueChanges.pipe(takeUntilDestroyed()).subscribe((status) => this.navigate({ status }));
    }

    protected statusTone(status: UserStatus): StatusTone {
        return status === UserStatus.ACTIVE ? StatusTone.SUCCESS : StatusTone.MUTED;
    }

    // Biến dòng của mat-table / matMenuContent có kiểu any -> tra nhãn qua hàm có kiểu
    protected statusLabel(status: UserStatus): string {
        return USER_STATUS_LABELS[status];
    }

    protected actionLabel(action: UserAction): string {
        return USER_ACTION_LABELS[action];
    }

    protected actionIcon(action: UserAction): string {
        return USER_ACTION_ICONS[action];
    }

    protected isDanger(action: UserAction): boolean {
        return DANGER_USER_ACTIONS.has(action);
    }

    protected onPage(event: PageEvent): void {
        this.navigate({ page: event.pageIndex, size: event.pageSize }, false);
    }

    protected onSort(sort: Sort): void {
        this.navigate({ sort: toSortParam(sort) });
    }

    protected clearFilters(): void {
        this.searchControl.setValue('', { emitEvent: false });
        this.orgUnitControl.setValue(null, { emitEvent: false });
        this.roleControl.setValue(null, { emitEvent: false });
        this.statusControl.setValue(null, { emitEvent: false });
        this.navigate({ q: '', orgUnitId: null, roleId: null, status: null });
    }

    protected openCreate(): void {
        this.openDialog(UserFormDialog, { mode: UserFormMode.CREATE }, DialogWidth.WIDE);
    }

    // Menu dòng dựng chỉ từ allowedActions
    protected runAction(action: UserAction, user: UserResponse): void {
        switch (action) {
            case UserAction.EDIT:
                this.openDialog(UserFormDialog, { mode: UserFormMode.EDIT, user }, DialogWidth.WIDE);
                return;
            case UserAction.RESET_PASSWORD:
                this.resetPassword(user);
                return;
            case UserAction.CHANGE_AVATAR:
                this.changeAvatar(user);
                return;
            case UserAction.REMOVE_AVATAR:
                this.removeAvatar(user);
                return;
            case UserAction.DEACTIVATE:
                this.openDialog(UserDeactivateDialog, { user }, DialogWidth.MEDIUM);
                return;
            case UserAction.REACTIVATE:
                this.openDialog(UserReactivateDialog, { user }, DialogWidth.MEDIUM);
                return;
        }
    }

    // Đổi bộ lọc / sắp xếp -> về trang đầu
    private navigate(patch: Partial<UserListQuery>, resetPage = true): void {
        const next: UserListQuery = { ...this.query(), ...(resetPage ? { page: 0 } : {}), ...patch };
        void this.router.navigate([], { relativeTo: this.route, queryParams: toUserListParams(next), replaceUrl: true });
    }

    // Hộp thoại đóng với true -> nạp lại trang hiện tại
    private openDialog<C, D>(component: ComponentType<C>, data: D, width: DialogWidth): void {
        this.dialog.open<C, D, boolean>(component, appDialogConfig(data, width, this.viewContainerRef))
            .afterClosed()
            .pipe(filter((changed) => changed === true))
            .subscribe(() => this.store.reload());
    }

    private resetPassword(user: UserResponse): void {
        const self = this.session.isSelf(user.id);
        confirmAction(this.dialog, {
            title: UserMessage.RESET_PASSWORD_TITLE,
            message: withSessionNotice(UserMessage.RESET_PASSWORD_NOTICE, self),
            confirmLabel: UserMessage.RESET_PASSWORD_CONFIRM,
            tone: ConfirmTone.DANGER,
        }, this.viewContainerRef).pipe(
            filter(Boolean),
            switchMap(() => this.store.resetPassword(user.id)),
        ).subscribe({
            next: () => {
                this.toast.success(UserMessage.PASSWORD_RESET);
                // Token của chính mình vừa bị thu hồi
                if (self) {
                    this.session.clear();
                    void this.router.navigateByUrl(AppUrl.LOGIN);
                }
            },
            error: ignoreError,
        });
    }

    private changeAvatar(user: UserResponse): void {
        openAvatarUpload(this.dialog, {
            title: UserMessage.CHANGE_AVATAR_TITLE,
            upload: (file) => this.store.changeAvatar(user.id, file),
        }, this.viewContainerRef)
            .pipe(filter(Boolean))
            .subscribe(() => this.afterAvatarChange(user, UserMessage.AVATAR_CHANGED));
    }

    private removeAvatar(user: UserResponse): void {
        confirmAction(this.dialog, {
            title: UserMessage.REMOVE_AVATAR_TITLE,
            message: UserMessage.REMOVE_AVATAR_NOTICE,
            confirmLabel: UserMessage.REMOVE_AVATAR_CONFIRM,
            tone: ConfirmTone.DANGER,
        }, this.viewContainerRef).pipe(
            filter(Boolean),
            switchMap(() => this.store.removeAvatar(user.id)),
        ).subscribe({
            next: () => this.afterAvatarChange(user, UserMessage.AVATAR_REMOVED),
            error: ignoreError,
        });
    }

    // Ảnh của chính mình -> header đổi ngay
    private afterAvatarChange(user: UserResponse, message: UserMessage): void {
        this.toast.success(message);
        this.store.reload();
        if (this.session.isSelf(user.id)) {
            this.session.refreshMe();
        }
    }
}