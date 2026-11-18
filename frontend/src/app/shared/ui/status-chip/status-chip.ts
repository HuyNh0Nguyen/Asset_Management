import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { StatusTone } from "./status-tone";

// Chip trạng thái dùng chung (đơn vị, người dùng, tài sản) - không biết miền
@Component({
    selector: 'app-status-chip',
    template: `<span [class]="chipClass()">{{ label() }}</span>`,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusChip {

    readonly label = input.required<string>();
    readonly tone = input<StatusTone>(StatusTone.MUTED);

    protected readonly chipClass = computed(() => `status-chip status-chip-${this.tone()}`);
}