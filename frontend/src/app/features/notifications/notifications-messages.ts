export enum NotificationListMessage {
    PAGE_TITLE = 'Tất cả thông báo',
    UNREAD_ONLY = 'Chỉ chưa đọc',
    EMPTY_UNREAD = 'Không có thông báo chưa đọc',
    ALL_READ = 'Bạn đã đọc hết thông báo',
    UNREAD_COUNT_SUFFIX = 'thông báo chưa đọc',
}

export function unreadSummary(count: number): string {
    return count > 0 ? `${count} ${NotificationListMessage.UNREAD_COUNT_SUFFIX}` : NotificationListMessage.ALL_READ;
}