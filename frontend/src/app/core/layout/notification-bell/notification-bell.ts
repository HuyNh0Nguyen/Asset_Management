import { ChangeDetectionStrategy, Component, computed, inject } from "@angular/core";
import { MatBadge } from '@angular/material/badge';
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { MatMenu, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { MatProgressBar } from "@angular/material/progress-bar";
import { Router, RouterLink } from "@angular/router";
import { NotificationResponse } from "@api/models";
import { NotificationItem } from "@core/notifications/notification-item/notification-item";
import { bellAriaLabel, NotificationMessage, openNotificationLink } from "@core/notifications/notification-util";
import { NotificationStore } from "@core/notifications/notification.store";
import { AppUrl } from "@core/routing/route.path";

// Lỗi HTTP đã được error.interceptor xử lý - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

@Component({
    selector: 'app-notification-bell',
    imports: [
        RouterLink,
        MatIconButton,
        MatButton,
        MatIcon,
        MatBadge,
        MatMenu,
        MatMenuItem,
        MatMenuTrigger,
        MatProgressBar,
        NotificationItem,
    ],
    templateUrl: './notification-bell.html',
    styleUrl: './notification-bell.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationBell {

    private readonly store = inject(NotificationStore);
    private readonly router = inject(Router);

    protected readonly unreadCount = this.store.unreadCount;
    protected readonly badgeText = this.store.badgeText;
    protected readonly latest = this.store.latest;
    protected readonly loadingLatest= this.store.loadingLatest;
    protected readonly markingAll = this.store.markingAll;
    protected readonly ariaLabel = computed(() => bellAriaLabel(this.unreadCount()));
    protected readonly allUrl = AppUrl.NOTIFICATIONS;
    protected readonly message = NotificationMessage;

    // Mở menu: nạp 10 mới nhất + đếm lại
    protected onMenuOpened(): void {
        this.store.loadLatest();
        this.store.refreshCount();
    }

    // Đánh dấu đã đọc rồi mở link trong ứng dụng (nếu có)
    protected open(notification: NotificationResponse): void {
        this.store.markRead(notification).subscribe({ error: ignoreError });
        openNotificationLink(this.router, notification.link);
    }

    // Giữ menu mở để thấy các mục chuyển sang đã đọc
    protected markAllRead(event: MouseEvent): void {
        event.stopPropagation();
        this.store.markAllRead().subscribe({ error: ignoreError });
    }
}