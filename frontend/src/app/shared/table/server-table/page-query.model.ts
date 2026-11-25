import { Sort, SortDirection } from "@angular/material/sort";

export const PAGE_SIZE_OPTIONS: number[] = [10, 20, 50];
// Khớp PageDefaults.DEFAULT_SIZE của backend
export const DEFAULT_PAGE_SIZE = 20;

export enum SortOrder {
    ASC = 'asc',
    DESC = 'desc',
}

// Trang đếm từ 0 như PageResponse
export interface PageQuery {
    page: number;
    size: number;
    sort: string | null;    // "<thuộc tính>,<asc|desc>" - đúng định dạng tham số sort của Spring
}

export function toSortParam(sort: Sort): string | null {
    return sort.active && sort.direction ? `${sort.active},${sort.direction}` : null;
}

export function parseSort(param: string | null): Sort {
    const [active = '', direction = ''] = (param ?? '').split(',');
    const valid = direction === SortOrder.ASC || direction === SortOrder.DESC;
    return { active, direction: (valid ? direction : '') as SortDirection };
}

export function readPageIndex(value: string | null): number {
    const page = Number(value);
    return Number.isInteger(page) && page > 0 ? page : 0;
}

export function readPageSize(value: string | null): number {
    const size = Number(value);
    return PAGE_SIZE_OPTIONS.includes(size) ? size : DEFAULT_PAGE_SIZE;
}