import { BreakpointObserver } from "@angular/cdk/layout";
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from "@angular/core";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { MatListItem, MatListItemIcon, MatListItemTitle, MatNavList } from "@angular/material/list";
import { MatMenu, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from "@angular/material/sidenav";
import { MatToolbar } from "@angular/material/toolbar";
import { ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { takeUntilDestroyed, toSignal } from "@angular/core/rxjs-interop";
import { BrandImage, LayoutBreakpoint, ShellMessage } from "../layout-breakpoint";
import { filter, finalize, map, skip } from "rxjs";
import { SessionStore } from "@core/auth/session.store";
import { AppUrl, RouteDataKey } from "@core/routing/route.path";
import { Permission } from "@api/models";
import { ToastService } from "@shared/ui/toast/toast.service";
import { Avatar } from "@shared/media/avatar/avatar";
import { AvatarSize } from "@shared/media/avatar-size";

// Quyền khai ở data của mọi route trên đường dẫn hiện tại
function requiredPermissions(root: ActivatedRouteSnapshot): Permission[] {
    const result: Permission[] = [];
    let current: ActivatedRouteSnapshot | null = root;
    while (current) {
        const permission = current.data[RouteDataKey.PERMISSION] as Permission | undefined;
        if (permission) {
            result.push(permission);
        }
        current = current.firstChild;
    }
    return result;
}

@Component({
    selector: 'app-shell',
    imports: [
        RouterOutlet,
        RouterLink,
        RouterLinkActive,
        MatToolbar,
        MatIcon,
        MatButton,
        MatIconButton,
        MatMenu,
        MatMenuItem,
        MatMenuTrigger,
        MatSidenav,
        MatSidenavContainer,
        MatSidenavContent,
        MatNavList,
        MatListItem,
        MatListItemIcon,
        MatListItemTitle,
        Avatar,
    ],
    templateUrl: './shell.html',
    styleUrl: './shell.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {

    private readonly breakpoints = inject(BreakpointObserver);
    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);
    private readonly toast = inject(ToastService);

    // Menu lọc theo quyền - đổi quyền thì menu đổi ở lần điều hướng kế tiếp
    protected readonly menuItems = this.session.visibleMenuItems;
    protected readonly fullName = computed(() => this.session.me()?.fullName ?? '');
    protected readonly avatarUrl = computed(() => this.session.me()?.avatarUrl);
    protected readonly avatarSize = AvatarSize.SMALL;
    protected readonly myAccountUrl = AppUrl.MY_ACCOUNT;
    protected readonly changePasswordUrl = AppUrl.CHANGE_PASSWORD;
    protected readonly brandLogo = BrandImage.LOGO;
    protected readonly loggingOut = signal(false);

    // true trên màn hình rộng; iPad và điện thoại dùng menu trượt
    protected readonly isDesktop = toSignal(
        this.breakpoints.observe(LayoutBreakpoint.DESKTOP).pipe(map((state) => state.matches)),
        { initialValue: false },
    );

    constructor() {
        // Mỗi lần chuyển trang làm mới /api/me; lần điều hướng đầu đã nạp ở authGuard
        this.router.events
            .pipe(filter((event) => event instanceof NavigationEnd), skip(1), takeUntilDestroyed())
            .subscribe(() => this.session.refreshMe());
        // Quyền vừa mất mà vẫn đứng ở trang cần quyền đó -> về trang chủ
        effect(() => {
            if (this.session.me()) {
                untracked(() => this.leaveIfForbidden());
            }
        })
    }

    // Màn hình nhỏ: chọn mục xong thì đóng menu
    protected onNavigate(drawer: MatSidenav): void {
        if (!this.isDesktop()) {
            void drawer.close();
        }
    }

    // Store luôn xóa phiên dù API lỗi
    protected logout(): void {
        this.loggingOut.set(true);
        this.session.logout()
            .pipe(finalize(() => this.loggingOut.set(false)))
            .subscribe(() => void this.router.navigateByUrl(AppUrl.LOGIN));
    }

    private leaveIfForbidden(): void {
        const permissions = requiredPermissions(this.router.routerState.snapshot.root);
        if (permissions.every((permission) => this.session.hasPermission(permission))) {
            return;
        }
        this.toast.warning(ShellMessage.PERMISSION_CHANGED);
        void this.router.navigateByUrl(this.session.homeUrl());
    }
}