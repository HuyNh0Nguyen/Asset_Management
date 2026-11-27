import { Router } from "@angular/router";
import { NotificationEventType } from "@api/models";

// Chuông làm mới bằng polling - không WebSocket / SSE
export const NOTIFICATION_POLL_INTERVAL_MS = 60_000;
// Số mục trong menu chuông
export const NOTIFICATION_LATEST_SIZE = 10;
// Quá ngưỡng thì badge hiện "99+"
export const NOTIFICATION_BADGE_MAX = 99;

// Câu chữ dùng chung cho chuông và trang "Tất cả thông báo"
export enum NotificationMessage {
    TITLE = 'Thông báo',
    MARK_ALL_READ = 'Đánh dấu tất cả đã đọc',
    MARK_ALL_READ_SUCCESS = 'Đã đánh dâu tất cả thông báo là đã đọc',
    EMPTY = 'Bạn chưa có thông báo nào',
    VIEW_ALL = 'Xem tất cả',
    UNREAD = 'Chưa đọc',
    UNREAD_SUFFIX = 'chưa đọc',
}

// "Thông báo, 5 chưa đọc" cho trình đọc màn hình
export function bellAriaLabel(unreadCount: number): string {
    return unreadCount > 0
        ? `${NotificationMessage.TITLE}, ${unreadCount} ${NotificationMessage.UNREAD_SUFFIX}`
        : NotificationMessage.TITLE;
}

// Icon Material theo sự kiện - Record: thiếu icon cho mã mới là lỗi biên dịch
export const NOTIFICATION_EVENT_ICONS: Record<NotificationEventType, string> = {
    [NotificationEventType.TASK_ACTIVATED]: 'pending_actions',
    [NotificationEventType.REQUEST_APPROVED]: 'check_circle',
    [NotificationEventType.REQUEST_REJECTED]: 'cancel',
    [NotificationEventType.REQUEST_STOPPED]: 'block',
    [NotificationEventType.ASSET_HANDED_OVER]: 'inventory_2',
    [NotificationEventType.ASSET_RECOVERED]: 'assignment_return',
    [NotificationEventType.HOLDER_REQUEST]: 'forward_to_inbox',
    [NotificationEventType.TASK_REASSIGNED]: 'swap_horiz',
    [NotificationEventType.TASK_UNRESOLVED]: 'person_off',
    [NotificationEventType.RECOVERY_DUE_REMINDER]: 'event_upcoming',
    [NotificationEventType.RECOVERY_OVERDUE_DIGEST]: 'event_busy',
    [NotificationEventType.TASK_OVERDUE_REMINDER]: 'alarm',
    [NotificationEventType.TASK_OVERDUE_DIGEST]: 'summarize',
    [NotificationEventType.APPROVED_REQUEST_CLOSED]: 'task_alt',
    [NotificationEventType.RECOVERY_EXTENDED]: 'update',
    [NotificationEventType.HELD_ASSET_MAINTENANCE]: 'build',
    [NotificationEventType.REQUEST_STAGE_PASSED]: 'next_plan',
    [NotificationEventType.EXTENSION_REJECTED]: 'timer_off'
};

enum InAppLinkPrefix {
    ROOT = '/',
    PROTOCOL_RELATIVE = '//',
}

// Chỉ điều hướng trong ứng dụng: link bắt đầu bằng "/" nhưng không phải "//"
export function isInAppLink(link: string | null | undefined): link is string {
    return !!link && link.startsWith(InAppLinkPrefix.ROOT) && !link.startsWith(InAppLinkPrefix.PROTOCOL_RELATIVE);
}

// Không có link hợp lệ thì chỉ đánh dấu đã đọc, không điều hướng
export function openNotificationLink(router: Router, link: string | null | undefined): void {
    if (isInAppLink(link)) {
        void router.navigateByUrl(link);
    }
}