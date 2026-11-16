import { BreakpointObserver } from "@angular/cdk/layout";
import { ChangeDetectionStrategy, Component, computed, inject, signal } from "@angular/core";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { MatListItem, MatListItemIcon, MatListItemTitle, MatNavList } from "@angular/material/list";
import { MatMenu, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from "@angular/material/sidenav";
import { MatToolbar } from "@angular/material/toolbar";
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { toSignal } from "@angular/core/rxjs-interop";
import { BrandImage, LayoutBreakpoint } from "../layout-breakpoint";
import { finalize, map } from "rxjs";
import { SessionStore } from "@core/auth/session.store";
import { AppUrl } from "@core/routing/route.path";

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
    ],
    templateUrl: './shell.html',
    styleUrl: './shell.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {

    private readonly breakpoints = inject(BreakpointObserver);
    private readonly session = inject(SessionStore);
    private readonly router = inject(Router);

    // Menu lọc theo quyền - đổi quyền thì menu đổi ở request kế tiếp
    protected readonly menuItems = this.session.visibleMenuItems;
    protected readonly fullName = computed(() => this.session.me()?.fullName ?? '');
    protected readonly changePasswordUrl = AppUrl.CHANGE_PASSWORD;
    protected readonly brandLogo = BrandImage.LOGO;
    protected readonly loggingOut = signal(false);

    // true trên màn hình rộng; iPad và điện thoại dùng menu trượt
    protected readonly isDesktop = toSignal(
        this.breakpoints.observe(LayoutBreakpoint.DESKTOP).pipe(map((state) => state.matches)),
        { initialValue: false },
    );

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
}