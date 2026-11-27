import { computed, DestroyRef, DOCUMENT, effect, inject, Injectable, signal, untracked } from "@angular/core";
import { NotificationResponse } from "@api/models";
import { NotificationsService } from "@api/services";
import { SessionStore } from "@core/auth/session.store";
import { ToastService } from "@shared/ui/toast/toast.service";
import { catchError, defer, finalize, Observable, of, Subscription, tap, throwError, timer } from "rxjs";
import { NOTIFICATION_BADGE_MAX, NOTIFICATION_LATEST_SIZE, NOTIFICATION_POLL_INTERVAL_MS, NotificationMessage } from "./notification-util";
import { skipErrorToast } from "@core/http/http-context";

// Lỗi HTTP đã được error.interceptor xử lý - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

// Singleton: chuông (core) và trang "Tất cả thông báo" (feature) dùng chung
@Injectable({ providedIn: 'root' })
export class NotificationStore {

    private readonly api = inject(NotificationsService);
    private readonly session = inject(SessionStore);
    private readonly toast = inject(ToastService);
    private readonly document = inject(DOCUMENT);

    private readonly unreadCountState = signal(0);
    private readonly latestState = signal<NotificationResponse[]>([]);
    private readonly loadingLatestState = signal(false);
    private readonly markingAllState = signal(false);
    private readonly pageVisible = signal(!this.document.hidden);

    // Chỉ poll khi có phiên và không bị buộc đổi mật khẩu (API sẽ trả 403)
    private readonly pollingToken = computed(() => (this.session.mustChangePassword() ? null : this.session.token()));

    // Phiên mà state hiện tại thuộc về - đổi phiên thì xóa sạch
    private ownerToken: string | null = null;
    private countInFlight = false;
    private latestRequest: Subscription | null = null;

    readonly unreadCount = this.unreadCountState.asReadonly();
    readonly latest = this.latestState.asReadonly();
    readonly loadingLatest = this.loadingLatestState.asReadonly();
    readonly markingAll = this.markingAllState.asReadonly();
    readonly badgeText = computed(() => {
        const count = this.unreadCountState();
        return count > NOTIFICATION_BADGE_MAX ? `${NOTIFICATION_BADGE_MAX}+` : String(count);
    });

    constructor() {
        // Tab ẩn -> dừng polling; tab hiện lại -> gọi ngay
        const onVisibilityChange = (): void => this.pageVisible.set(!this.document.hidden);
        this.document.addEventListener('visibilitychange', onVisibilityChange);
        inject(DestroyRef).onDestroy(() => this.document.removeEventListener('visibilitychange', onVisibilityChange));

        // Polling: timer(0, 60s) theo phiên + độ hiển thị của tab
        effect((onCleanup) => {
            const token = this.pollingToken();
            const visible = this.pageVisible();
            untracked(() => this.switchOwner(token));
            if (!token || !visible) {
                return;
            }
            const subscription = timer(0, NOTIFICATION_POLL_INTERVAL_MS).subscribe(() => this.refreshCount());
            onCleanup(() => subscription.unsubscribe());
        });
    }

    // Không toast: mạng chập chờn không làm phiền; lỗi thì giữ số cũ
    refreshCount(): void {
        const token = this.pollingToken();
        if (!token || this.countInFlight) {
            return;
        }
        this.countInFlight = true;
        this.api.getUnreadNotificationCount(undefined, skipErrorToast())
            .pipe(finalize(() => (this.countInFlight = false)))
            .subscribe({
                next: (response) => {
                    if (this.isOwner(token)) {
                        this.unreadCountState.set(response.unreadCount);
                    }
                },
                error: ignoreError,
            });
    }

    // 10 mới nhất, cả đã đọc lẫn chưa đọc
    loadLatest(): void {
        const token = this.session.token();
        if (!token) {
            return;
        }
        this.latestRequest?.unsubscribe();
        this.loadingLatestState.set(true);
        this.latestRequest = this.api.listNotifications({ page: 0, size: NOTIFICATION_LATEST_SIZE })
            .pipe(finalize(() => this.loadingLatestState.set(false)))
            .subscribe({
                next: (page) => {
                    if (this.isOwner(token)) {
                        this.latestState.set(page.items);
                    }
                },
                error: ignoreError,
            });
    }

    // Lạc quan khi subsribe; lỗi -> hoàn tác số + trạng thái, error.interceptor toast
    markRead(notification: NotificationResponse): Observable<void> {
        if (notification.read) {
            return of(undefined);
        }
        return defer(() => {
            const token = this.session.token();
            this.applyRead(notification.id, true);
            this.unreadCountState.update((count) => Math.max(count - 1, 0));
            return this.api.markNotificationRead({ id: notification.id }).pipe(
                catchError((error: unknown) => {
                    if (this.isOwner(token)) {
                        this.applyRead(notification.id, false);
                        this.unreadCountState.update((count) => count + 1);
                    }
                    return throwError(() => error);
                }),
            );
        });
    }

    markAllRead(): Observable<void> {
        return defer(() => {
            const token = this.session.token();
            this.markingAllState.set(true);
            return this.api.markAllNotificationsRead().pipe(
                tap(() => {
                    if (!this.isOwner(token)) {
                        return;
                    }
                    this.unreadCountState.set(0);
                    this.latestState.update((items) => items.map((item) => (item.read ? item : { ...item, read: true })));
                    this.toast.success(NotificationMessage.MARK_ALL_READ_SUCCESS);
                }),
                finalize(() => this.markingAllState.set(false)),
            );
        });
    }

    // Đăng xuất / đổi người dùng -> dừng + xóa state, không để người sau thấy số của người trước
    private switchOwner(token: string | null): void {
        if (token === this.ownerToken) {
            return;
        }
        this.ownerToken = token;
        this.latestRequest?.unsubscribe();
        this.unreadCountState.set(0);
        this.latestState.set([]);
    }

    // Kết quả về muộn của phiên cũ bị bỏ
    private isOwner(token: string | null): boolean {
        return token !== null && this.session.token() === token;
    }

    private applyRead(id: string, read: boolean): void {
        this.latestState.update((items) => items.map((item) => (item.id === id ? { ...item, read } : item)));
    }
}