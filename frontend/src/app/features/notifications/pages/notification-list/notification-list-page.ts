import { ChangeDetectionStrategy, Component, computed, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { PageEvent } from "@angular/material/paginator";
import { MatSlideToggle } from "@angular/material/slide-toggle";
import { ActivatedRoute, Router } from "@angular/router";
import { NotificationResponse } from "@api/models";
import { NotificationItem } from "@core/notifications/notification-item/notification-item";
import { NotificationMessage, openNotificationLink } from "@core/notifications/notification-util";
import { NotificationStore } from "@core/notifications/notification.store";
import { readNotificationListQuery, toNotificationListQueryParams } from "../../data/notification-list-query";
import { NotificationListQuery } from "../../data/notification-list-query.model";
import { NotificationListStore } from "../../data/notification-list.store";
import { NotificationListMessage, unreadSummary } from "../../notifications-messages";
import { ServerTable } from "@shared/table/server-table/server-table";
import { map } from "rxjs";

@Component({
    selector: 'app-notification-list-page',
    imports: [MatButton, MatIcon, MatSlideToggle, ServerTable, NotificationItem],
    templateUrl: './notification-list-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationListPage {

    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly store = inject(NotificationListStore);
    private readonly notifications = inject(NotificationStore);

    protected readonly items = this.store.items;
    protected readonly total = this.store.total;
    protected readonly loading = this.store.loading;
    protected readonly query = this.store.query;
    protected readonly unreadCount = this.notifications.unreadCount;
    protected readonly markingAll = this.notifications.markingAll;
    protected readonly summary = computed(() => unreadSummary(this.unreadCount()));
    protected readonly emptyText = computed(() =>
        this.query().unreadOnly ? NotificationListMessage.EMPTY_UNREAD : NotificationMessage.EMPTY,
    );
    protected readonly message = NotificationListMessage;
    protected readonly notificationMessage = NotificationMessage;

    constructor() {
        // URL là nguồn của bộ lọc: tải lại trang vẫn giữ "Chỉ chưa đọc" và trang hiện tại
        this.route.queryParamMap
            .pipe(map(readNotificationListQuery), takeUntilDestroyed())
            .subscribe((query) => this.store.load(query));
    }

    protected onUnreadOnlyChange(unreadOnly: boolean): void {
        this.navigate({ ...this.query(), unreadOnly, page: 0 });
    }

    protected onPageChange(event: PageEvent): void {
        this.navigate({ ...this.query(), page: event.pageIndex, size: event.pageSize });
    }

    protected open(notification: NotificationResponse): void {
        this.store.markRead(notification);
        openNotificationLink(this.router, notification.link);
    }

    protected markAllRead(): void {
        this.store.markAllRead();
    }

    private navigate(query: NotificationListQuery): void {
        void this.router.navigate([], { relativeTo: this.route, queryParams: toNotificationListQueryParams(query) });
    }
}