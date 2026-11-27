import { DEFAULT_PAGE_SIZE } from "@shared/table/server-table/page-query.model";

// Bộ lọc trang "Tất cả thông báo" - đồng bộ với query params
export interface NotificationListQuery {
    unreadOnly: boolean;
    page: number;
    size: number;
}

export const DEFAULT_NOTIFICATION_LIST_QUERY: NotificationListQuery = {
    unreadOnly: false,
    page: 0,
    size: DEFAULT_PAGE_SIZE,
};