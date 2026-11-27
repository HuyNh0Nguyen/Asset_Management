import { ParamMap, Params } from "@angular/router";
import { NotificationListQuery } from "./notification-list-query.model";
import { QueryParamKey } from "@core/routing/route.path";
import { DEFAULT_PAGE_SIZE, readPageIndex, readPageSize } from "@shared/table/server-table/page-query.model";

// Giá trị cờ trên URL: ?unread=true
export enum QueryFlag {
    TRUE = 'true',
}

export function readNotificationListQuery(params: ParamMap): NotificationListQuery {
    return {
        unreadOnly: params.get(QueryParamKey.UNREAD) === QueryFlag.TRUE,
        page: readPageIndex(params.get(QueryParamKey.PAGE)),
        size: readPageSize(params.get(QueryParamKey.SIZE)),
    };
}

// Giá trị mặc định -> null để URL gọn
export function toNotificationListQueryParams(query: NotificationListQuery): Params {
    return {
        [QueryParamKey.UNREAD]: query.unreadOnly ? QueryFlag.TRUE : null,
        [QueryParamKey.PAGE]: query.page > 0 ? query.page : null,
        [QueryParamKey.SIZE]: query.size !== DEFAULT_PAGE_SIZE ? query.size : null,
    };
}