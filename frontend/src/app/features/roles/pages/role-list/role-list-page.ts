import { BreakpointObserver } from "@angular/cdk/layout";
import { ChangeDetectionStrategy, Component, computed, inject, signal, ViewContainerRef } from "@angular/core";
import { toSignal } from "@angular/core/rxjs-interop";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatButtonToggle, MatButtonToggleGroup } from "@angular/material/button-toggle";
import { MatDialog } from "@angular/material/dialog";
import { MatIcon } from "@angular/material/icon";
import { MatMenu, MatMenuContent, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { MatProgressBar } from "@angular/material/progress-bar";
import { MatTableModule } from "@angular/material/table";
import { Router, RouterLink } from "@angular/router";
import { Permission, RoleAction, RoleResponse } from "@api/models";
import { DATA_SCOPE_LABELS, PERMISSION_LABELS, ROLE_ACTION_LABELS } from "@core/i18n/labels.vi";
import { ViewportQuery } from "@core/layout/layout-breakpoint";
import { AppUrl } from "@core/routing/route.path";
import { groupDefinitions } from "../../data/permission-groups";
import { DESKTOP_ROLE_COLUMNS, PHONE_ROLE_COLUMNS, ROLE_ACTION_ICONS, RoleListColumn, RoleListView } from "../../data/role-ui";
import { RolesStore } from "../../data/roles.store";
import { deleteRoleMessage, RoleMessage } from "../../roles-messages";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { ConfirmTone } from "@shared/ui/confirm-dialog/confirm-dialog.model";
import { StatusChip } from "@shared/ui/status-chip/status-chip";
import { StatusTone } from "@shared/ui/status-chip/status-tone";
import { ToastService } from "@shared/ui/toast/toast.service";
import { filter, map, switchMap } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

@Component({
    selector: 'app-role-list-page',
    imports: [
        RouterLink,
        MatTableModule,
        MatButton,
        MatIconButton,
        MatIcon,
        MatMenu,
        MatMenuItem,
        MatMenuTrigger,
        MatMenuContent,
        MatButtonToggleGroup,
        MatButtonToggle,
        MatProgressBar,
        StatusChip,
    ],
    templateUrl: './role-list-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleListPage {

    private readonly store = inject(RolesStore);
    private readonly router = inject(Router);
    private readonly dialog = inject(MatDialog);
    private readonly viewContainerRef = inject(ViewContainerRef);
    private readonly toast = inject(ToastService);
    private readonly breakpoints = inject(BreakpointObserver);

    protected readonly roles = this.store.roles;
    protected readonly loading = this.store.loading;
    protected readonly view = signal(RoleListView.LIST);
    protected readonly viewOption = RoleListView;
    protected readonly column = RoleListColumn;
    protected readonly rolesUrl = AppUrl.ROLES;
    protected readonly newRoleUrl = AppUrl.ROLE_NEW;
    protected readonly permissionLabels = PERMISSION_LABELS;
    protected readonly systemChip = RoleMessage.SYSTEM_CHIP;
    protected readonly systemTone = StatusTone.INFO;
    protected readonly groups = computed(() => groupDefinitions(this.store.permissions()));
    protected readonly deleteAction = RoleAction.DELETE;
    protected readonly notGranted = RoleMessage.NOT_GRANTED;

    private readonly isPhone = toSignal(
        this.breakpoints.observe(ViewportQuery.PHONE).pipe(map((state) => state.matches)), { initialValue: false }
    );
    protected readonly displayedColumns = computed(() => (this.isPhone() ? PHONE_ROLE_COLUMNS : DESKTOP_ROLE_COLUMNS));

    // vai trò -> (quyền -> phạm vi) cho ma trận tổng
    private readonly grantsByRole = computed(() => new Map(this.roles().map((role) =>
        [role.id, new Map(role.grants.map((grant) => [grant.permission, grant.scope]))])));

    constructor() {
        this.store.load();
    }

    protected scopeLabel(roleId: string, permission: Permission): string {
        const scope = this.grantsByRole().get(roleId)?.get(permission);
        return scope ? DATA_SCOPE_LABELS[scope] : RoleMessage.NOT_GRANTED;
    }

    // role / action trong matMenuContent có kiểu any -> tra qua hàm có kiểu
    protected actionLabel(action: RoleAction): string {
        return ROLE_ACTION_LABELS[action];
    }

    protected actionIcon(action: RoleAction): string {
        return ROLE_ACTION_ICONS[action];
    }

    protected onViewChange(value: RoleListView): void {
        this.view.set(value);
    }

    protected openRole(roleId: string): void {
        void this.router.navigate([this.rolesUrl, roleId]);
    }

    protected runAction(action: RoleAction, role: RoleResponse): void {
        if (action === RoleAction.EDIT) {
            void this.router.navigate([AppUrl.ROLES, role.id]);
            return;
        }
        this.delete(role);
    }

    // Không đếm trước: RESTRICT ở CSDL là quy tắc -> 422 ROLE_IN_USE do interceptor toast
    private delete(role: RoleResponse): void {
        confirmAction(this.dialog, {
            title: RoleMessage.DELETE_TITLE,
            message: deleteRoleMessage(role.name),
            confirmLabel: RoleMessage.DELETE_CONFIRM,
            tone: ConfirmTone.DANGER,
        }, this.viewContainerRef).pipe(
            filter(Boolean),
            switchMap(() => this.store.delete(role.id)),
        ).subscribe({
            next: () => {
                this.toast.success(RoleMessage.ROLE_DELETED);
                this.store.load();
            },
            error: ignoreError,
        });
    }
}