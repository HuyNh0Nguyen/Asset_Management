import { DatePipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { MatIcon } from "@angular/material/icon";
import { MatTooltip } from "@angular/material/tooltip";
import { NotificationResponse } from "@api/models";
import { FULL_DATE_TIME_FORMAT, RelativeTimePipe } from "@shared/text/relative-time";
import { NOTIFICATION_EVENT_ICONS, NotificationMessage } from "../notification-util";

// Chỉ hiển thị - thao tác bấm do nơi chứa xử lý
@Component({
    selector: 'app-notification-item',
    imports: [MatIcon, MatTooltip, DatePipe, RelativeTimePipe],
    templateUrl: './notification-item.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationItem {

    readonly notification = input.required<NotificationResponse>();
    // Cắt nội dung 2 dòng (menu chuông)
    readonly clamp = input(false);

    protected readonly icon = computed(() => NOTIFICATION_EVENT_ICONS[this.notification().eventType]);
    protected readonly fullDateTimeFormat = FULL_DATE_TIME_FORMAT;
    protected readonly unreadLabel = NotificationMessage.UNREAD;
}