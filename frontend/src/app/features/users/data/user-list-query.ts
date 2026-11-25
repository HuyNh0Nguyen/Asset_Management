import { ParamMap, Params } from "@angular/router";
import { ListUsers$Params } from "@api/functions";
import { UserResponse, UserStatus } from "@api/models";
import { QueryParamKey } from "@core/routing/route.path";
import { DEFAULT_PAGE_SIZE, PageQuery, readPageIndex, readPageSize } from "@shared/table/server-table/page-query.model";

// Bộ lọc nằm trên query string - F5 / chia sẻ link giữ nguyên
export interface UserListQuery extends PageQuery {
    q: string;
    orgUnitId: string | null;
    roleId: string | null;
    status: UserStatus | null;
}

// Dữ liệu chung của hộp thoại vô hiệu hóa / kích hoạt lại
export interface UserDialogData {
    user: UserResponse;
}

function readStatus(value: string | null): UserStatus | null {
    return (Object.values(UserStatus) as string[]).includes(value ?? '') ? value as UserStatus : null;
}

export function readUserListQuery(params: ParamMap): UserListQuery {
    return {
        q: params.get(QueryParamKey.SEARCH) ?? '',
        orgUnitId: params.get(QueryParamKey.ORG_UNIT_ID),
        roleId: params.get(QueryParamKey.ROLE_ID),
        status: readStatus(params.get(QueryParamKey.STATUS)),
        page: readPageIndex(params.get(QueryParamKey.PAGE)),
        size: readPageSize(params.get(QueryParamKey.SIZE)),
        sort: params.get(QueryParamKey.SORT),
    };
}

// Bỏ giá trị mặc định cho URL gọn; null = xóa khỏi query string
export function toUserListParams(query: UserListQuery): Params {
    return {
        [QueryParamKey.SEARCH]: query.q.trim() || null,
        [QueryParamKey.ORG_UNIT_ID]: query.orgUnitId,
        [QueryParamKey.ROLE_ID]: query.roleId,
        [QueryParamKey.STATUS]: query.status,
        [QueryParamKey.PAGE]: query.page > 0 ? query.page : null,
        [QueryParamKey.SIZE]: query.size !== DEFAULT_PAGE_SIZE ? query.size : null,
        [QueryParamKey.SORT]: query.sort,
    };
}

export function toListUsersParams(query: UserListQuery): ListUsers$Params {
    return {
        q: query.q.trim() || undefined,
        orgUnitId: query.orgUnitId ?? undefined,
        roleId: query.roleId ?? undefined,
        status: query.status ?? undefined,
        page: query.page,
        size: query.size,
        sort: query.sort ? [query.sort] : undefined,
    };
}