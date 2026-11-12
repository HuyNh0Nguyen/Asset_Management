import { BreakpointObserver } from "@angular/cdk/layout";
import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { MatIconButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { MatListItem, MatListItemIcon, MatListItemTitle, MatNavList } from "@angular/material/list";
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from "@angular/material/sidenav";
import { MatToolbar } from "@angular/material/toolbar";
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { MENU_ITEMS } from "../menu-items";
import { toSignal } from "@angular/core/rxjs-interop";
import { BrandImage, LayoutBreakpoint } from "../layout-breakpoint";
import { map } from "rxjs";

@Component({
    selector: 'app-shell',
    imports: [
        RouterOutlet,
        RouterLink,
        RouterLinkActive,
        MatToolbar,
        MatIcon,
        MatIconButton,
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

    protected readonly menuItems = MENU_ITEMS;
    protected readonly brandLogo = BrandImage.LOGO;

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
}