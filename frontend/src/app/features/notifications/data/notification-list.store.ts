import { inject, Injectable, signal } from "@angular/core";
import { NotificationResponse } from "@api/models";
import { NotificationsService } from "@api/services";
import { NotificationStore } from "@core/notifications/notification.store";
import { DEFAULT_NOTIFICATION_LIST_QUERY, NotificationListQuery } from "./notification-list-query.model";
import { finalize, Subscription } from "rxjs";

// Lỗi HTTP đã được error.interceptor xử lý - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

// Scoped theo route notifications
@Injectable()
export class NotificationListStore {

    private readonly api = inject(NotificationsService);
    private readonly notifications = inject(NotificationStore);

    private readonly itemsState = signal<NotificationResponse[]>([]);
    private readonly totalState = signal(0);
    private readonly loadingState = signal(false);
    private readonly queryState = signal<NotificationListQuery>(DEFAULT_NOTIFICATION_LIST_QUERY);
    private request: Subscription | null = null;

    readonly items = this.itemsState.asReadonly();
    readonly total = this.totalState.asReadonly();
    readonly loading = this.loadingState.asReadonly();
    readonly query = this.queryState.asReadonly();

    // Bỏ kết quả của lần lọc cũ khi lọc mới
    load(query: NotificationListQuery): void {
        this.queryState.set(query);
        this.request?.unsubscribe();
        this.loadingState.set(true);
        this.request = this.api.listNotifications({ unreadOnly: query.unreadOnly, page: query.page, size: query.size })
            .pipe(finalize(() => this.loadingState.set(false)))
            .subscribe({
                next: (page) => {
                    this.itemsState.set(page.items);
                    this.totalState.set(page.totalElements);
                    // Badge trên header khớp với trang vừa tải
                    this.notifications.refreshCount();
                },
                error: ignoreError,
            });
    }

    reload(): void {
        this.load(this.queryState());
    }

    // Lạc quan trên dòng; số chưa đọc do NotificationStore lo
    markRead(notification: NotificationResponse): void {
        if (notification.read) {
            return;
        }
        this.setRead(notification.id, true);
        this.notifications.markRead(notification).subscribe({ error: () => this.setRead(notification.id, false) });
    }

    markAllRead(): void {
        this.notifications.markAllRead().subscribe({ next: () => this.reload(), error: ignoreError });
    }

    private setRead(id: string, read: boolean): void {
        this.itemsState.update((items) => items.map((item) => (item.id === id ? { ...item, read } : item)));
    }
}