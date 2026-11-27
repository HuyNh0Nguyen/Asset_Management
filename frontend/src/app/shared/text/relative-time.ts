import { Pipe, PipeTransform } from "@angular/core";

// Intl cho ra "5 phút trước", "hôm qua", "26/11/2026"
const DISPLAY_LOCALE = 'vi-VN';

// Giờ đầy đủ cho tooltip (dùng với DatePipe)
export const FULL_DATE_TIME_FORMAT = 'HH:mm dd/MM/yyyy';

const MINUTE_MS = 60_000;
const HOUR_MS = 60 * MINUTE_MS;
const DAY_MS = 24 * HOUR_MS;
// Quá số ngày này thì hiện ngày tuyệt đối
const RELATIVE_DAY_LIMIT = 7;

export enum RelativeTimeText {
    JUST_NOW = 'vừa xong',
}

const relativeFormat = new Intl.RelativeTimeFormat(DISPLAY_LOCALE, { numeric: 'auto' });
const dateFormat = new Intl.DateTimeFormat(DISPLAY_LOCALE, { day: '2-digit', month: '2-digit', year: 'numeric' });

// Hàm thuần - dùng được ngoài template
export function formatRelativeTime(value: string | Date | null | undefined, now: Date = new Date()): string {
    if (!value) {
        return '';
    }
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) {
        return '';
    }
    const elapsed = now.getTime() - date.getTime();
    // Dưới 1 phút (kể cả giờ máy khách lệch về sau) -> "vừa xong"
    if (elapsed < MINUTE_MS) {
        return RelativeTimeText.JUST_NOW;
    }
    if (elapsed < HOUR_MS) {
        return relativeFormat.format(-Math.floor(elapsed / MINUTE_MS), 'minute');
    }
    const days = calendarDaysBetween(date, now);
    if (days === 0) {
        return relativeFormat.format(-Math.floor(elapsed / HOUR_MS), 'hour');
    }
    // 1 ngày -> "hôm qua" (numeric: 'auto')
    if (days <= RELATIVE_DAY_LIMIT) {
        return relativeFormat.format(-days, 'day');
    }
    return dateFormat.format(date);
}

// Số ngày lịch giữa hai thời điểm (không phải số lần 24 giờ)
function calendarDaysBetween(from: Date, to: Date): number {
    const startOf = (date: Date): number => new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
    return Math.round((startOf(to) - startOf(from)) / DAY_MS);
}

@Pipe({ name: 'relativeTime' })
export class RelativeTimePipe implements PipeTransform {
    transform(value: string | Date | null | undefined): string {
        return formatRelativeTime(value);
    }
}